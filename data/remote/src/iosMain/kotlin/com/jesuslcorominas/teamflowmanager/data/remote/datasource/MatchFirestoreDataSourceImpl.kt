package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.MatchFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Match
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

    private fun documentToMatch(
        docId: String,
        model: MatchFirestoreModel,
        teamDocId: String,
    ): Match = model.copy(id = docId, teamId = teamDocId).toDomain()

    /**
     * Finds the Firestore document ID for a given domain match ID (Long).
     * Fetches all team matches and returns the document whose stable ID matches.
     */
    private suspend fun findDocumentIdByMatchId(
        teamDocId: String,
        matchId: Long,
    ): String? {
        return try {
            val snapshot =
                firestore.collection(MATCHES_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .get()
            snapshot.documents.firstOrNull { doc ->
                doc.id.toStableId() == matchId
            }?.id
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override fun getMatchesByTeam(teamFirestoreId: String): Flow<List<Match>> =
        flow {
            val snapshots =
                firestore.collection(MATCHES_COLLECTION)
                    .where { "teamId" equalTo teamFirestoreId }
                    .snapshots
            emitAll(
                snapshots.map { qs ->
                    qs.documents.mapNotNull { doc ->
                        try {
                            val model = doc.data<MatchFirestoreModel>()
                            documentToMatch(doc.id, model, teamFirestoreId)
                        } catch (_: Exception) {
                            null
                        }
                    }
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                },
            )
        }

    override fun getAllMatches(): Flow<List<Match>> =
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
                firestore.collection(MATCHES_COLLECTION)
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
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                },
            )
        }

    override fun getArchivedMatches(): Flow<List<Match>> =
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
                firestore.collection(MATCHES_COLLECTION)
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
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                },
            )
        }

    override fun getMatchById(matchId: Long): Flow<Match?> =
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
            val snapshots =
                firestore.collection(MATCHES_COLLECTION)
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
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(null) else throw e
                },
            )
        }

    override suspend fun getScheduledMatches(): List<Match> {
        val teamDocId = getTeamDocumentId() ?: return emptyList()
        return try {
            val snapshot =
                firestore.collection(MATCHES_COLLECTION)
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

    override suspend fun insertMatch(match: Match): Long {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("No team found for current user")
        val model = match.toFirestoreModel().copy(teamId = teamDocId)
        return try {
            val docRef = firestore.collection(MATCHES_COLLECTION).add(model)
            docRef.id.toStableId()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateMatch(match: Match) {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("No team found for current user")
        val documentId =
            findDocumentIdByMatchId(teamDocId, match.id)
                ?: throw IllegalStateException("Cannot find Firestore document for match ${match.id}")
        val model = match.toFirestoreModel().copy(id = documentId, teamId = teamDocId)
        try {
            firestore.collection(MATCHES_COLLECTION).document(documentId).set(model)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deleteMatch(matchId: Long) {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("No team found for current user")
        val documentId =
            findDocumentIdByMatchId(teamDocId, matchId)
                ?: throw IllegalStateException("Cannot find Firestore document for match $matchId")
        try {
            firestore.collection(MATCHES_COLLECTION).document(documentId).delete()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateMatchCaptain(
        matchId: Long,
        captainId: Long?,
    ) {
        val teamDocId = getTeamDocumentId() ?: return
        val documentId = findDocumentIdByMatchId(teamDocId, matchId) ?: return
        try {
            firestore.collection(MATCHES_COLLECTION).document(documentId)
                .update("captainId" to (captainId ?: 0L))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best-effort — not critical if this fails silently
        }
    }

    override suspend fun getAllMatchesDirect(): List<Match> = emptyList()

    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
