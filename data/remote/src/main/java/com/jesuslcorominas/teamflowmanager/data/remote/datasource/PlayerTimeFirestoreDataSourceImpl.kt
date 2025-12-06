package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
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
        private const val TAG = "PlayerTimeFirestoreDS"
        private const val PLAYER_TIMES_COLLECTION = "playerTimes"
        private const val TEAMS_COLLECTION = "teams"
    }

    /**
     * Gets the team's Firestore document ID for the current authenticated user.
     * This is needed because security rules validate player time access based on team ownership.
     */
    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid
        Log.d(TAG, "getTeamDocumentId: currentUserId=$currentUserId")

        if (currentUserId == null) {
            Log.w(TAG, "getTeamDocumentId: No authenticated user")
            return null
        }

        return try {
            Log.d(TAG, "getTeamDocumentId: Querying Firestore for team with ownerId=$currentUserId")
            val snapshot = firestore.collection(TEAMS_COLLECTION)
                .whereEqualTo("ownerId", currentUserId)
                .limit(1)
                .get()
                .await()

            val teamDocId = snapshot.documents.firstOrNull()?.id
            Log.d(TAG, "getTeamDocumentId: Found teamDocId=$teamDocId")
            teamDocId
        } catch (e: CancellationException) {
            Log.w(TAG, "getTeamDocumentId: Query was cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getTeamDocumentId: Error getting team document ID", e)
            null
        }
    }

    /**
     * Gets player time for a specific player as a real-time Flow.
     */
    override fun getPlayerTime(playerId: Long): Flow<PlayerTime?> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "getPlayerTime: No authenticated user (playerId=$playerId)")
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "getPlayerTime: No team found for user (playerId=$playerId)")
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        Log.d(TAG, "getPlayerTime: teamDocId=$teamDocId, playerId=$playerId")

        // Use playerId as document ID for easy retrieval
        val docId = "player_$playerId"
        val listenerRegistration = firestore.collection(PLAYER_TIMES_COLLECTION)
            .document(docId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getPlayerTime: Error from Firestore", error)
                    trySend(null)
                    return@addSnapshotListener
                }

                val playerTime = snapshot?.toObject(PlayerTimeFirestoreModel::class.java)?.let {
                    // Verify this belongs to the user's team
                    if (it.teamId == teamDocId) {
                        it.toDomain()
                    } else {
                        Log.w(TAG, "getPlayerTime: PlayerTime teamId mismatch")
                        null
                    }
                }

                Log.d(TAG, "getPlayerTime: Found playerTime for playerId=$playerId: ${playerTime != null}")
                trySend(playerTime)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Gets all player times for the current user's team from Firestore as a real-time Flow.
     */
    override fun getAllPlayerTimes(): Flow<List<PlayerTime>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "getAllPlayerTimes: No authenticated user")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "getAllPlayerTimes: No team found for user")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        Log.d(TAG, "getAllPlayerTimes: teamDocId=$teamDocId")

        val listenerRegistration = firestore.collection(PLAYER_TIMES_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getAllPlayerTimes: Error from Firestore", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val playerTimes = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PlayerTimeFirestoreModel::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "getAllPlayerTimes: Loaded ${playerTimes.size} player times for team $teamDocId")
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
        Log.d(TAG, "upsertPlayerTime: Starting upsert for playerId=${playerTime.playerId}")

        val teamDocId = getTeamDocumentId()
        Log.d(TAG, "upsertPlayerTime: Got teamDocId=$teamDocId")

        if (teamDocId == null) {
            Log.e(TAG, "upsertPlayerTime: No team found, cannot upsert player time - user may not be authenticated")
            throw IllegalStateException("Team must exist to upsert player time")
        }

        // Use playerId as document ID
        val docId = "player_${playerTime.playerId}"
        Log.d(TAG, "upsertPlayerTime: Using document ID: $docId")

        val firestoreModel = playerTime.toFirestoreModel()
        val modelWithTeam = firestoreModel.copy(
            id = docId,
            teamId = teamDocId,
        )

        Log.d(TAG, "upsertPlayerTime: Setting document in Firestore...")
        try {
            firestore.collection(PLAYER_TIMES_COLLECTION)
                .document(docId)
                .set(modelWithTeam)
                .await()
            Log.d(TAG, "upsertPlayerTime: PlayerTime upserted successfully with id: $docId, teamId: $teamDocId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            Log.e(TAG, "Firestore PERMISSION_DENIED or ERROR: ${e.code} - ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "General error upserting player time: ${e.message}", e)
            throw e
        }
    }

    /**
     * Deletes all player times for the current user's team from Firestore.
     * This is typically called when finishing a match.
     */
    override suspend fun deleteAllPlayerTimes() {
        Log.d(TAG, "deleteAllPlayerTimes: Starting delete all")

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "deleteAllPlayerTimes: No team found, cannot delete player times - user may not be authenticated")
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
                Log.d(TAG, "deleteAllPlayerTimes: Deleted document ${document.id}")
            }

            Log.d(TAG, "deleteAllPlayerTimes: Deleted ${snapshot.documents.size} player times for team $teamDocId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "deleteAllPlayerTimes: Error deleting player times from Firestore", e)
            throw e
        }
    }
}
