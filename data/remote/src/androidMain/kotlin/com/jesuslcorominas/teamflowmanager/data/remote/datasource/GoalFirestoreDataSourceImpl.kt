package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.GoalFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

// Pre-migration documents stored matchId/scorerId/playerId as Long (hash of Firestore doc ID).
// This function reimplements the deleted toStableId() so we can query both old and new formats.
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
 * Firestore-based implementation of GoalDataSource.
 */
class GoalFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : GoalDataSource {
    companion object {
        private const val GOALS_COLLECTION = "goals"
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

    override fun getMatchGoals(matchId: String): Flow<List<Goal>> =
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
                firestore.collection(GOALS_COLLECTION)
                    .whereEqualTo("teamId", teamDocId)
                    .whereIn("matchId", listOf(matchId, matchId.toLegacyId()))
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val goals =
                            snapshot?.documents?.mapNotNull { document ->
                                try {
                                    val rawData = document.data ?: return@mapNotNull null
                                    val rawMatchId = rawData["matchId"]?.toString() ?: ""
                                    val rawScorerId = rawData["scorerId"]?.toString()
                                    val model =
                                        try {
                                            document.toObject(GoalFirestoreModel::class.java)
                                                ?: return@mapNotNull null
                                        } catch (_: Exception) {
                                            GoalFirestoreModel(
                                                teamId = rawData["teamId"] as? String ?: teamDocId,
                                                matchId = rawMatchId,
                                                scorerId = rawScorerId,
                                                goalTimeMillis =
                                                    rawData["goalTimeMillis"] as? Long
                                                        ?: 0L,
                                                matchElapsedTimeMillis =
                                                    rawData["matchElapsedTimeMillis"] as? Long
                                                        ?: 0L,
                                                isOpponentGoal =
                                                    rawData["opponentGoal"] as? Boolean ?: false,
                                                isOwnGoal = rawData["ownGoal"] as? Boolean ?: false,
                                            )
                                        }
                                    model
                                        .copy(
                                            id = document.id,
                                            matchId = matchId,
                                            scorerId = rawScorerId,
                                        )
                                        .toDomain()
                                } catch (_: Exception) {
                                    null
                                }
                            } ?: emptyList()
                        trySend(goals)
                    }

            awaitClose { listenerRegistration.remove() }
        }

    override fun getAllTeamGoals(): Flow<List<Goal>> =
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
                firestore.collection(GOALS_COLLECTION)
                    .whereEqualTo("teamId", teamDocId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val goals =
                            snapshot?.documents?.mapNotNull { document ->
                                document.toObject(GoalFirestoreModel::class.java)?.toDomain()
                            } ?: emptyList()
                        trySend(goals)
                    }

            awaitClose { listenerRegistration.remove() }
        }

    override suspend fun insertGoal(goal: Goal): String {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to create a goal")

        val docRef = firestore.collection(GOALS_COLLECTION).document()
        val firestoreModel = goal.toFirestoreModel().copy(id = docRef.id, teamId = teamDocId)

        try {
            docRef.set(firestoreModel).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getAllGoalsDirect(): List<Goal> = emptyList()

    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
