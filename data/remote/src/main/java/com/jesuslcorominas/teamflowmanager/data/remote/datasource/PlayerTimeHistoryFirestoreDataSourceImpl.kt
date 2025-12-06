package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
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
        private const val TAG = "PlayerTimeHistoryFirestoreDS"
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
                    Log.d(TAG, "findMatchDocumentId: Found match document ID: $docId for matchId: $matchId")
                    return docId
                }
            }
            Log.w(TAG, "findMatchDocumentId: No match found for matchId: $matchId")
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "findMatchDocumentId: Error finding match document ID", e)
            null
        }
    }

    /**
     * Gets player time history for a specific player as a real-time Flow.
     */
    override fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistory>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "getPlayerTimeHistory: No authenticated user (playerId=$playerId)")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "getPlayerTimeHistory: No team found for user (playerId=$playerId)")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        Log.d(TAG, "getPlayerTimeHistory: teamDocId=$teamDocId, playerId=$playerId")

        val listenerRegistration = firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("playerId", playerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getPlayerTimeHistory: Error from Firestore", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val history = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PlayerTimeHistoryFirestoreModel::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "getPlayerTimeHistory: Loaded ${history.size} history entries for player $playerId")
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
            Log.w(TAG, "getMatchPlayerTimeHistory: No authenticated user (matchId=$matchId)")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "getMatchPlayerTimeHistory: No team found for user (matchId=$matchId)")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        Log.d(TAG, "getMatchPlayerTimeHistory: teamDocId=$teamDocId, matchId=$matchId")

        val listenerRegistration = firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("matchId", matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getMatchPlayerTimeHistory: Error from Firestore", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val history = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PlayerTimeHistoryFirestoreModel::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "getMatchPlayerTimeHistory: Loaded ${history.size} history entries for match $matchId")
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
            Log.w(TAG, "getAllPlayerTimeHistory: No authenticated user")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "getAllPlayerTimeHistory: No team found for user")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        Log.d(TAG, "getAllPlayerTimeHistory: teamDocId=$teamDocId")

        val listenerRegistration = firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getAllPlayerTimeHistory: Error from Firestore", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val history = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PlayerTimeHistoryFirestoreModel::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "getAllPlayerTimeHistory: Loaded ${history.size} history entries for team $teamDocId")
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
        Log.d(TAG, "insertPlayerTimeHistory: Starting insert for playerId=${playerTimeHistory.playerId}, matchId=${playerTimeHistory.matchId}")

        val teamDocId = getTeamDocumentId()
        Log.d(TAG, "insertPlayerTimeHistory: Got teamDocId=$teamDocId")

        if (teamDocId == null) {
            Log.e(TAG, "insertPlayerTimeHistory: No team found, cannot insert player time history - user may not be authenticated")
            throw IllegalStateException("Team must exist to create player time history")
        }

        // Find the match document ID for security rules
        // If we can't find it, use empty string and rely on teamId validation in security rules
        val matchDocId = findMatchDocumentId(teamDocId, playerTimeHistory.matchId)
        if (matchDocId == null) {
            Log.w(TAG, "insertPlayerTimeHistory: No match document found for matchId=${playerTimeHistory.matchId}, continuing with empty matchDocId")
        }

        val docRef = firestore.collection(PLAYER_TIME_HISTORY_COLLECTION).document()
        Log.d(TAG, "insertPlayerTimeHistory: Created document reference with id=${docRef.id}")

        val firestoreModel = playerTimeHistory.toFirestoreModel()
        val modelWithTeam = firestoreModel.copy(
            id = docRef.id,
            teamId = teamDocId,
            matchDocId = matchDocId ?: "",
        )

        Log.d(TAG, "insertPlayerTimeHistory: Setting document in Firestore...")
        try {
            docRef.set(modelWithTeam).await()
            Log.d(TAG, "insertPlayerTimeHistory: PlayerTimeHistory inserted successfully with id: ${docRef.id}, teamId: $teamDocId, matchDocId: ${matchDocId ?: "empty"}")
            return docRef.id.toStableId()
        } catch (e: CancellationException) {
            throw e
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            Log.e(TAG, "Firestore PERMISSION_DENIED or ERROR: ${e.code} - ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "General error inserting player time history: ${e.message}", e)
            throw e
        }
    }
}
