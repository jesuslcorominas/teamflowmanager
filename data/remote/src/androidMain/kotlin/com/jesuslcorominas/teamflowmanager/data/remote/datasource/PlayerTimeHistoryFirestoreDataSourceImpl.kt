package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerTimeHistoryFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

// Pre-migration documents stored playerId/matchId as Long (hash of Firestore doc ID).
private fun String.toLegacyId(): Long {
    var result = 0L
    var multiplier = 1L
    for (char in this) {
        result += char.code.toLong() * multiplier
        multiplier *= 31L
    }
    return kotlin.math.abs(result)
}

/**
 * Firestore-based implementation of PlayerTimeHistoryDataSource.
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

    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return null
        return try {
            val snapshot =
                firestore.collection(TEAMS_COLLECTION)
                    .whereEqualTo("assignedCoachId", currentUserId)
                    .limit(1)
                    .get()
                    .await()
            snapshot.documents.firstOrNull()?.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    // For non-coach roles (e.g. president), derive teamId from the match document.
    private suspend fun getTeamDocumentIdOrFromMatch(matchId: String): String? =
        getTeamDocumentId()
            ?: try {
                firestore.collection(MATCHES_COLLECTION).document(matchId).get().await()
                    .getString("teamId")
            } catch (_: Exception) {
                null
            }

    override fun getPlayerTimeHistory(playerId: String): Flow<List<PlayerTimeHistory>> =
        callbackFlow {
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

            val listenerRegistration =
                firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
                    .whereEqualTo("teamId", teamDocId)
                    .whereIn("playerId", listOf(playerId, playerId.toLegacyId()))
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val history =
                            snapshot?.documents?.mapNotNull { document ->
                                try {
                                    val rawData = document.data ?: return@mapNotNull null
                                    val rawPlayerId = rawData["playerId"]?.toString() ?: ""
                                    val rawMatchId = rawData["matchId"]?.toString() ?: ""
                                    val model =
                                        try {
                                            document.toObject(
                                                PlayerTimeHistoryFirestoreModel::class.java,
                                            ) ?: return@mapNotNull null
                                        } catch (_: Exception) {
                                            PlayerTimeHistoryFirestoreModel(
                                                teamId = rawData["teamId"] as? String ?: teamDocId,
                                                playerId = rawPlayerId,
                                                matchId = rawMatchId,
                                                elapsedTimeMillis =
                                                    rawData["elapsedTimeMillis"] as? Long ?: 0L,
                                                savedAtMillis =
                                                    rawData["savedAtMillis"] as? Long ?: 0L,
                                            )
                                        }
                                    model
                                        .copy(
                                            id = document.id,
                                            playerId = rawPlayerId,
                                            matchId = rawMatchId,
                                        )
                                        .toDomain()
                                } catch (_: Exception) {
                                    null
                                }
                            } ?: emptyList()
                        trySend(history)
                    }

            awaitClose { listenerRegistration.remove() }
        }

    override fun getMatchPlayerTimeHistory(matchId: String): Flow<List<PlayerTimeHistory>> =
        callbackFlow {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            val teamDocId = getTeamDocumentIdOrFromMatch(matchId)
            if (teamDocId == null) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            val listenerRegistration =
                firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
                    .whereEqualTo("teamId", teamDocId)
                    .whereIn("matchId", listOf(matchId, matchId.toLegacyId()))
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val history =
                            snapshot?.documents?.mapNotNull { document ->
                                try {
                                    val rawData = document.data ?: return@mapNotNull null
                                    val rawPlayerId = rawData["playerId"]?.toString() ?: ""
                                    val model =
                                        try {
                                            document.toObject(
                                                PlayerTimeHistoryFirestoreModel::class.java,
                                            ) ?: return@mapNotNull null
                                        } catch (_: Exception) {
                                            PlayerTimeHistoryFirestoreModel(
                                                teamId = rawData["teamId"] as? String ?: teamDocId,
                                                playerId = rawPlayerId,
                                                matchId = matchId,
                                                elapsedTimeMillis =
                                                    rawData["elapsedTimeMillis"] as? Long ?: 0L,
                                                savedAtMillis =
                                                    rawData["savedAtMillis"] as? Long ?: 0L,
                                            )
                                        }
                                    model
                                        .copy(
                                            id = document.id,
                                            matchId = matchId,
                                            playerId = rawPlayerId,
                                        )
                                        .toDomain()
                                } catch (_: Exception) {
                                    null
                                }
                            } ?: emptyList()
                        trySend(history)
                    }

            awaitClose { listenerRegistration.remove() }
        }

    override fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistory>> =
        callbackFlow {
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

            val listenerRegistration =
                firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
                    .whereEqualTo("teamId", teamDocId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val history =
                            snapshot?.documents?.mapNotNull { document ->
                                document.toObject(PlayerTimeHistoryFirestoreModel::class.java)?.toDomain()
                            } ?: emptyList()
                        trySend(history)
                    }

            awaitClose { listenerRegistration.remove() }
        }

    override suspend fun insertPlayerTimeHistory(playerTimeHistory: PlayerTimeHistory): String {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to create player time history")

        val docRef = firestore.collection(PLAYER_TIME_HISTORY_COLLECTION).document()
        val firestoreModel = playerTimeHistory.toFirestoreModel().copy(id = docRef.id, teamId = teamDocId)

        try {
            docRef.set(firestoreModel).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getAllPlayerTimeHistoryDirect(): List<PlayerTimeHistory> = emptyList()

    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
