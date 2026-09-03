package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.parseGoalDocument
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.utils.toLegacyId
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

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
                    .where { "assignedCoachId" equalTo currentUserId }
                    .limit(1)
                    .get()
            snapshot.documents.firstOrNull()?.id
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getTeamDocumentIdOrFromMatch(matchId: String): String? =
        getTeamDocumentId()
            ?: try {
                val doc = firestore.collection(MATCHES_COLLECTION).document(matchId).get()
                if (!doc.exists) {
                    null
                } else {
                    doc.data<Map<String, Any?>>()["teamId"] as? String
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }

    override fun getMatchGoals(matchId: String): Flow<List<Goal>> =
        flow {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                emit(emptyList())
                return@flow
            }
            val teamDocId = getTeamDocumentIdOrFromMatch(matchId)
            if (teamDocId == null) {
                emit(emptyList())
                return@flow
            }
            // Combine two real-time listeners: one for new String-ID docs, one for legacy Long-ID docs.
            // Each source is mapped to a List and given its own .catch BEFORE the combine, so a
            // failure on the legacy query (e.g. missing composite index) does not blank out
            // valid new-doc data (#385.3).
            // TODO: remove legacy branch after backward-compat window closes.
            val newGoals =
                firestore.collection(GOALS_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .where { "matchId" equalTo matchId }
                    .snapshots
                    .map { qs ->
                        qs.documents.mapNotNull { doc ->
                            try {
                                parseGoalDocument(doc.data<Map<String, Any?>>(), doc.id, matchId)
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }.catch { e ->
                        if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                    }
            val legacyGoals =
                firestore.collection(GOALS_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .where { "matchId" equalTo matchId.toLegacyId() }
                    .snapshots
                    .map { qs ->
                        qs.documents.mapNotNull { doc ->
                            try {
                                parseGoalDocument(doc.data<Map<String, Any?>>(), doc.id, matchId)
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }.catch { e ->
                        if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                    }
            emitAll(combine(newGoals, legacyGoals) { a, b -> a + b })
        }

    override fun getAllTeamGoals(): Flow<List<Goal>> =
        flow {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                emit(emptyList())
                return@flow
            }
            val teamDocId = getTeamDocumentId()
            if (teamDocId == null) {
                emit(emptyList())
                return@flow
            }
            val snapshots =
                firestore.collection(GOALS_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .snapshots
            emitAll(
                snapshots.map { qs ->
                    qs.documents.mapNotNull { doc ->
                        try {
                            val rawData = doc.data<Map<String, Any?>>()
                            val rawMatchId = rawData["matchId"]?.toString() ?: ""
                            parseGoalDocument(rawData, doc.id, rawMatchId)
                        } catch (_: Exception) {
                            null
                        }
                    }
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                },
            )
        }

    override suspend fun insertGoal(goal: Goal): String {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to insert goal")
        return try {
            val model = goal.toFirestoreModel().copy(teamId = teamDocId)
            val docRef = firestore.collection(GOALS_COLLECTION).add(model)
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getAllGoalsDirect(): List<Goal> = emptyList()

    override suspend fun clearLocalData() = Unit
}
