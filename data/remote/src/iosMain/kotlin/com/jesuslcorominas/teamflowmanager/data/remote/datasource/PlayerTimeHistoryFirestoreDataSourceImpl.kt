package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.parsePlayerTimeHistoryDocument
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.util.toLegacyId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
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

    override fun getPlayerTimeHistory(playerId: String): Flow<List<PlayerTimeHistory>> =
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
            // Combine two real-time listeners: one for new String-ID docs, one for legacy Long-ID docs.
            // Each source is mapped to a List and given its own .catch BEFORE the combine, so a
            // failure on the legacy query does not blank out valid new-doc data (#385.3).
            // TODO: remove legacy branch after backward-compat window closes.
            val newHistory =
                firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .where { "playerId" equalTo playerId }
                    .snapshots
                    .map { qs ->
                        qs.documents.mapNotNull { doc ->
                            val rawData = doc.data<Map<String, Any?>>()
                            val rawMatchId = rawData["matchId"]?.toString() ?: ""
                            parsePlayerTimeHistoryDocument(rawData, doc.id, teamDocId, playerId, rawMatchId)
                        }
                    }.catch { e ->
                        if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                    }
            val legacyHistory =
                firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .where { "playerId" equalTo playerId.toLegacyId() }
                    .snapshots
                    .map { qs ->
                        qs.documents.mapNotNull { doc ->
                            val rawData = doc.data<Map<String, Any?>>()
                            val rawMatchId = rawData["matchId"]?.toString() ?: ""
                            parsePlayerTimeHistoryDocument(rawData, doc.id, teamDocId, playerId, rawMatchId)
                        }
                    }.catch { e ->
                        if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                    }
            emitAll(combine(newHistory, legacyHistory) { a, b -> a + b })
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

    override fun getMatchPlayerTimeHistory(matchId: String): Flow<List<PlayerTimeHistory>> =
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
            // failure on the legacy query does not blank out valid new-doc data (#385.3).
            // TODO: remove legacy branch after backward-compat window closes.
            val newHistory =
                firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .where { "matchId" equalTo matchId }
                    .snapshots
                    .map { qs ->
                        qs.documents.mapNotNull { doc ->
                            val rawData = doc.data<Map<String, Any?>>()
                            val rawPlayerId = rawData["playerId"]?.toString() ?: ""
                            parsePlayerTimeHistoryDocument(rawData, doc.id, teamDocId, rawPlayerId, matchId)
                        }
                    }.catch { e ->
                        if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                    }
            val legacyHistory =
                firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .where { "matchId" equalTo matchId.toLegacyId() }
                    .snapshots
                    .map { qs ->
                        qs.documents.mapNotNull { doc ->
                            val rawData = doc.data<Map<String, Any?>>()
                            val rawPlayerId = rawData["playerId"]?.toString() ?: ""
                            parsePlayerTimeHistoryDocument(rawData, doc.id, teamDocId, rawPlayerId, matchId)
                        }
                    }.catch { e ->
                        if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                    }
            emitAll(combine(newHistory, legacyHistory) { a, b -> a + b })
        }

    override fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistory>> =
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
                firestore.collection(PLAYER_TIME_HISTORY_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .snapshots
            emitAll(
                snapshots.map { qs ->
                    qs.documents.mapNotNull { doc ->
                        val rawData = doc.data<Map<String, Any?>>()
                        val rawPlayerId = rawData["playerId"]?.toString() ?: ""
                        val rawMatchId = rawData["matchId"]?.toString() ?: ""
                        parsePlayerTimeHistoryDocument(rawData, doc.id, teamDocId, rawPlayerId, rawMatchId)
                    }
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                },
            )
        }

    override suspend fun insertPlayerTimeHistory(playerTimeHistory: PlayerTimeHistory): String {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to insert player time history")
        return try {
            val model = playerTimeHistory.toFirestoreModel().copy(teamId = teamDocId)
            val docRef = firestore.collection(PLAYER_TIME_HISTORY_COLLECTION).add(model)
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getAllPlayerTimeHistoryDirect(): List<PlayerTimeHistory> = emptyList()

    override suspend fun clearLocalData() = Unit
}
