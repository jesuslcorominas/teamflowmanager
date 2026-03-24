package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerTimeHistoryFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of PlayerTimeHistoryDataSource.
 * This implementation stores player time history data in Firebase Firestore as a remote data source.
 * PlayerTimeHistory documents are stored in the "playerTimeHistory" collection with auto-generated document IDs.
 * The teamId field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
class PlayerTimeHistoryFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : PlayerTimeHistoryDataSource {

    companion object {
        private const val PLAYER_TIME_HISTORY_COLLECTION = "playerTimeHistory"
        private const val TEAMS_COLLECTION = "teams"
        private const val MATCHES_COLLECTION = "matches"
    }

    /**
     * Gets the team's Firestore document ID for the current authenticated user.
     * This is needed because security rules validate player time history access based on team ownership.
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
     * Helper function to find the Firestore document ID for a match based on the Long match ID.
     */
    private suspend fun findMatchDocumentId(teamDocId: String, matchId: Long): String? {
        return try {
            val snapshot = firestore.collection(MATCHES_COLLECTION)
                .whereEqualTo("teamId", teamDocId)
                .get()
                .await()

            for (document in snapshot.documents) {
                // Check if this match's stable ID matches
                val docId = document.id
                if (docId.toStableId() == matchId) {
                    return docId
                }
            }
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets player time history for a specific player as a real-time Flow.
     */
    override fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistory>> = callbackFlow {
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

        val listenerRegistration = firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("playerId", playerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val history = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PlayerTimeHistoryFirestoreModel::class.java)?.toDomain()
                } ?: emptyList()

                trySend(history)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Gets player time history for a specific match as a real-time Flow.
     */
    override fun getMatchPlayerTimeHistory(matchId: Long): Flow<List<PlayerTimeHistory>> = callbackFlow {
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

        val listenerRegistration = firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("matchId", matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val history = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PlayerTimeHistoryFirestoreModel::class.java)?.toDomain()
                } ?: emptyList()

                trySend(history)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Gets all player time history for the current user's team from Firestore as a real-time Flow.
     */
    override fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistory>> = callbackFlow {
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

        val listenerRegistration = firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val history = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PlayerTimeHistoryFirestoreModel::class.java)?.toDomain()
                } ?: emptyList()

                trySend(history)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Inserts a new player time history entry into Firestore.
     * Returns a stable Long ID derived from the Firestore document ID.
     */
    override suspend fun insertPlayerTimeHistory(playerTimeHistory: PlayerTimeHistory): Long {
        val teamDocId = getTeamDocumentId()

        if (teamDocId == null) {
            throw IllegalStateException("Team must exist to create player time history")
        }

        // Find the match document ID for security rules
        // If we can't find it, use empty string and rely on teamId validation in security rules
        val matchDocId = findMatchDocumentId(teamDocId, playerTimeHistory.matchId)

        val docRef = firestore.collection(PLAYER_TIME_HISTORY_COLLECTION).document()

        val firestoreModel = playerTimeHistory.toFirestoreModel()
        val modelWithTeam = firestoreModel.copy(
            id = docRef.id,
            teamId = teamDocId,
            matchDocId = matchDocId ?: "",
        )

        try {
            docRef.set(modelWithTeam).await()
            return docRef.id.toStableId()
        } catch (e: CancellationException) {
            throw e
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * This method is not applicable for remote Firestore data source.
     * @return empty list as direct access is not needed for remote storage
     */
    override suspend fun getAllPlayerTimeHistoryDirect(): List<PlayerTimeHistory> = emptyList()

    /**
     * This method is not applicable for remote Firestore data source.
     * Only relevant for local Room database cleanup.
     */
    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
