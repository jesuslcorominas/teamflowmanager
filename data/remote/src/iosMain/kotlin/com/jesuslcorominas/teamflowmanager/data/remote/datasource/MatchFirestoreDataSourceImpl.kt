package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.MatchFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class MatchFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : MatchDataSource {

    companion object {
        private const val MATCHES_COLLECTION = "matches"
        private const val TEAMS_COLLECTION = "teams"
    }

    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return null
        return try {
            val snapshot = firestore.collection(TEAMS_COLLECTION)
                .where { "ownerId" equalTo currentUserId }
                .limit(1)
                .get()
            snapshot.documents.firstOrNull()?.id
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private fun documentToMatch(docId: String, model: MatchFirestoreModel, teamDocId: String): Match =
        model.copy(id = docId, teamId = teamDocId).toDomain()

    override fun getAllMatches(): Flow<List<Match>> = flow {
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
        val snapshots = firestore.collection(MATCHES_COLLECTION)
            .where { "teamId" equalTo teamDocId }
            .where { "archived" equalTo false }
            .snapshots
        emitAll(
            snapshots.map { qs ->
                qs.documents.mapNotNull { doc ->
                    try {
                        val model = doc.data<MatchFirestoreModel>()
                        documentToMatch(doc.id, model, teamDocId)
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        )
    }

    override fun getArchivedMatches(): Flow<List<Match>> = flow {
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
        val snapshots = firestore.collection(MATCHES_COLLECTION)
            .where { "teamId" equalTo teamDocId }
            .where { "archived" equalTo true }
            .snapshots
        emitAll(
            snapshots.map { qs ->
                qs.documents.mapNotNull { doc ->
                    try {
                        val model = doc.data<MatchFirestoreModel>()
                        documentToMatch(doc.id, model, teamDocId)
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        )
    }

    override fun getMatchById(matchId: Long): Flow<Match?> = flow {
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
        val snapshots = firestore.collection(MATCHES_COLLECTION)
            .where { "teamId" equalTo teamDocId }
            .snapshots
        emitAll(
            snapshots.map { qs ->
                qs.documents.mapNotNull { doc ->
                    try {
                        val model = doc.data<MatchFirestoreModel>()
                        documentToMatch(doc.id, model, teamDocId)
                    } catch (_: Exception) {
                        null
                    }
                }.find { it.id == matchId }
            }
        )
    }

    override suspend fun getScheduledMatches(): List<Match> {
        val teamDocId = getTeamDocumentId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection(MATCHES_COLLECTION)
                .where { "teamId" equalTo teamDocId }
                .where { "archived" equalTo false }
                .where { "status" equalTo "SCHEDULED" }
                .get()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val model = doc.data<MatchFirestoreModel>()
                    documentToMatch(doc.id, model, teamDocId)
                } catch (_: Exception) {
                    null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Write operations not implemented for iOS Phase 2 MVP

    override suspend fun insertMatch(match: Match): Long =
        throw NotImplementedError("insertMatch not implemented for iOS Phase 2")

    override suspend fun updateMatch(match: Match) =
        throw NotImplementedError("updateMatch not implemented for iOS Phase 2")

    override suspend fun deleteMatch(matchId: Long) =
        throw NotImplementedError("deleteMatch not implemented for iOS Phase 2")

    override suspend fun updateMatchCaptain(matchId: Long, captainId: Long?) =
        throw NotImplementedError("updateMatchCaptain not implemented for iOS Phase 2")

    override suspend fun getAllMatchesDirect(): List<Match> = emptyList()

    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
