package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerTimeFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of PlayerTimeDataSource.
 * This implementation stores current player time data in Firebase Firestore as a remote data source.
 * PlayerTime documents are stored in the "playerTimes" collection with playerId as the document ID.
 * The teamId field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
class PlayerTimeFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : PlayerTimeDataSource {

    companion object {
        private const val PLAYER_TIMES_COLLECTION = "playerTimes"
        private const val TEAMS_COLLECTION = "teams"
    }

    /**
     * Gets the team's Firestore document ID for the current authenticated user.
     * This is needed because security rules validate player time access based on team ownership.
     */
    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId == null) {
            return null
        }

        return try {
            val snapshot = firestore.collection(TEAMS_COLLECTION)
                .whereEqualTo("assignedCoachId", currentUserId)
                .limit(1)
                .get()
                .await()

            val teamDocId = snapshot.documents.firstOrNull()?.id
            teamDocId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets player time for a specific player as a real-time Flow.
     */
    override fun getPlayerTime(playerId: Long): Flow<PlayerTime?> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        // Use playerId as document ID for easy retrieval
        val docId = "player_$playerId"
        val listenerRegistration = firestore.collection(PLAYER_TIMES_COLLECTION)
            .document(docId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                try {
                    val model = snapshot.toObject(PlayerTimeFirestoreModel::class.java)
                    if (model == null) {
                        trySend(null)
                        return@addSnapshotListener
                    }

                    // Verify this belongs to the user's team
                    if (model.teamId != teamDocId) {
                        trySend(null)
                        return@addSnapshotListener
                    }

                    trySend(model.toDomain())
                } catch (e: Exception) {
                    trySend(null)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Gets player times scoped to a specific match from Firestore as a real-time Flow.
     * Documents from previous matches (matchId mismatch) are ignored automatically,
     * which prevents stale data from corrupting a new match even if deletion failed.
     *
     * Note: requires a composite Firestore index on playerTimes(teamId ASC, matchId ASC).
     */
    override fun getPlayerTimesByMatch(matchId: Long): Flow<List<PlayerTime>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(PLAYER_TIMES_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("matchId", matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val playerTimes = snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(PlayerTimeFirestoreModel::class.java)?.toDomain()
                    } catch (e: Exception) {
                        null
                    }
                }

                trySend(playerTimes)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Upserts (updates or inserts) a player time in Firestore.
     */
    override suspend fun upsertPlayerTime(playerTime: PlayerTime) {
        val teamDocId = getTeamDocumentId()

        if (teamDocId == null) {
            throw IllegalStateException("Team must exist to upsert player time")
        }

        // Use playerId as document ID
        val docId = "player_${playerTime.playerId}"

        val firestoreModel = playerTime.toFirestoreModel()
        val modelWithTeam = firestoreModel.copy(
            id = docId,
            teamId = teamDocId,
        )

        try {
            firestore.collection(PLAYER_TIMES_COLLECTION)
                .document(docId)
                .set(modelWithTeam)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Batch upserts multiple player times at once using Firestore batch write.
     * All operations complete atomically or fail together.
     */
    override suspend fun batchUpsertPlayerTimes(playerTimes: List<PlayerTime>) {
        if (playerTimes.isEmpty()) {
            return
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            throw IllegalStateException("Team must exist to upsert player times")
        }

        try {
            val batch = firestore.batch()

            playerTimes.forEach { playerTime ->
                val docId = "player_${playerTime.playerId}"
                val firestoreModel = playerTime.toFirestoreModel()
                val modelWithTeam = firestoreModel.copy(
                    id = docId,
                    teamId = teamDocId,
                )

                val docRef = firestore.collection(PLAYER_TIMES_COLLECTION).document(docId)
                batch.set(docRef, modelWithTeam)
            }

            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Deletes all player times for the current user's team from Firestore.
     * This is typically called when finishing a match.
     */
    override suspend fun deleteAllPlayerTimes() {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            return
        }

        try {
            // Get all player times for this team
            val snapshot = firestore.collection(PLAYER_TIMES_COLLECTION)
                .whereEqualTo("teamId", teamDocId)
                .get()
                .await()

            // Delete each document
            for (document in snapshot.documents) {
                document.reference.delete().await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * This method is not applicable for remote Firestore data source.
     * @return empty list as direct access is not needed for remote storage
     */
    override suspend fun getAllPlayerTimesDirect(): List<PlayerTime> = emptyList()

    /**
     * This method is not applicable for remote Firestore data source.
     * Only relevant for local Room database cleanup.
     */
    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
