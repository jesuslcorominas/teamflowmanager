package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerTimeFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class PlayerTimeFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : PlayerTimeDataSource {
    companion object {
        private const val PLAYER_TIMES_COLLECTION = "playerTimes"
        private const val TEAMS_COLLECTION = "teams"
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

    override fun getPlayerTime(playerId: Long): Flow<PlayerTime?> =
        flow {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                emit(null)
                return@flow
            }
            val teamDocId = getTeamDocumentId()
            if (teamDocId == null) {
                emit(null)
                return@flow
            }
            val docId = "player_$playerId"
            val snapshots =
                firestore.collection(PLAYER_TIMES_COLLECTION)
                    .document(docId)
                    .snapshots
            emitAll(
                snapshots.map { doc ->
                    if (!doc.exists) return@map null
                    try {
                        val model = doc.data<PlayerTimeFirestoreModel>()
                        if (model.teamId != teamDocId) null else model.toDomain()
                    } catch (_: Exception) {
                        null
                    }
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(null) else throw e
                },
            )
        }

    /**
     * Gets player times scoped to a specific match from Firestore as a real-time Flow.
     * Documents from previous matches (matchId mismatch) are ignored automatically,
     * which prevents stale data from corrupting a new match even if deletion failed.
     *
     * Note: requires a composite Firestore index on playerTimes(teamId ASC, matchId ASC).
     */
    override fun getPlayerTimesByMatch(matchId: Long): Flow<List<PlayerTime>> =
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
                firestore.collection(PLAYER_TIMES_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .where { "matchId" equalTo matchId }
                    .snapshots
            emitAll(
                snapshots.map { qs ->
                    qs.documents.mapNotNull { doc ->
                        try {
                            doc.data<PlayerTimeFirestoreModel>().toDomain()
                        } catch (_: Exception) {
                            null
                        }
                    }
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                },
            )
        }

    override suspend fun upsertPlayerTime(playerTime: PlayerTime) {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to upsert player time")
        val docId = "player_${playerTime.playerId}"
        val model = playerTime.toFirestoreModel().copy(teamId = teamDocId)
        try {
            firestore.collection(PLAYER_TIMES_COLLECTION).document(docId).set(model)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun batchUpsertPlayerTimes(playerTimes: List<PlayerTime>) {
        if (playerTimes.isEmpty()) return
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to upsert player times")
        try {
            val batch = firestore.batch()
            playerTimes.forEach { playerTime ->
                val docId = "player_${playerTime.playerId}"
                val model = playerTime.toFirestoreModel().copy(teamId = teamDocId)
                val docRef = firestore.collection(PLAYER_TIMES_COLLECTION).document(docId)
                batch.set(docRef, model)
            }
            batch.commit()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deleteAllPlayerTimes() {
        val teamDocId = getTeamDocumentId() ?: return
        try {
            val snapshot =
                firestore.collection(PLAYER_TIMES_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .get()
            snapshot.documents.forEach { doc ->
                firestore.collection(PLAYER_TIMES_COLLECTION).document(doc.id).delete()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // best-effort
        }
    }

    override suspend fun getAllPlayerTimesDirect(): List<PlayerTime> = emptyList()

    override suspend fun clearLocalData() = Unit
}
