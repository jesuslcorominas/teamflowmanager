package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.MatchFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.parseMatchDocument
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.DocumentSnapshot
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

    /**
     * Typed deserialization first, raw-map parsing as a fallback.
     *
     * A pre-migration document stores `captainId` as a Long and `squadCallUpIds` /
     * `startingLineupIds` as `List<Long>`, so `doc.data<MatchFirestoreModel>()` throws and the
     * whole match used to resolve to `null` — the president could not even open a legacy match.
     *
     * TODO: remove the legacy fallback after the backward-compat window closes.
     */
    private fun documentToMatch(
        doc: DocumentSnapshot,
        teamDocId: String,
    ): Match? =
        try {
            doc.data<MatchFirestoreModel>().copy(id = doc.id, teamId = teamDocId).toDomain()
        } catch (_: Exception) {
            parseMatchDocument(rawDataOrNull(doc), doc.id, teamDocId)
        }

    /** Reads the document as a raw map; returns null when even that fails. */
    private fun rawDataOrNull(doc: DocumentSnapshot): Map<String, Any?>? =
        try {
            doc.data<Map<String, Any?>>()
        } catch (_: Exception) {
            null
        }

    override fun getMatchesByTeam(teamId: String): Flow<List<Match>> =
        flow {
            val snapshots =
                firestore.collection(MATCHES_COLLECTION)
                    .where { "teamId" equalTo teamId }
                    .snapshots
            emitAll(
                snapshots.map { qs ->
                    qs.documents.mapNotNull { doc ->
                        documentToMatch(doc, teamId)
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
                        documentToMatch(doc, teamDocId)
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
                        documentToMatch(doc, teamDocId)
                    }
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                },
            )
        }

    override fun getMatchById(matchId: String): Flow<Match?> =
        flow {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                emit(null)
                return@flow
            }
            val snapshots = firestore.collection(MATCHES_COLLECTION).document(matchId).snapshots
            emitAll(
                snapshots.map { doc ->
                    if (!doc.exists) return@map null
                    val teamDocId = rawDataOrNull(doc)?.get("teamId") as? String ?: ""
                    documentToMatch(doc, teamDocId)
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
                documentToMatch(doc, teamDocId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun insertMatch(match: Match): String {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("No team found for current user")
        val model = match.toFirestoreModel().copy(id = "", teamId = teamDocId)
        return try {
            val docRef = firestore.collection(MATCHES_COLLECTION).add(model)
            docRef.id
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
        val model = match.toFirestoreModel().copy(id = match.id, teamId = teamDocId)
        try {
            firestore.collection(MATCHES_COLLECTION).document(match.id).set(model)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deleteMatch(matchId: String) {
        try {
            firestore.collection(MATCHES_COLLECTION).document(matchId).delete()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateMatchCaptain(
        matchId: String,
        captainId: String?,
    ) {
        try {
            firestore.collection(MATCHES_COLLECTION).document(matchId)
                .update("captainId" to (captainId ?: ""))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best-effort
        }
    }

    override suspend fun getAllMatchesDirect(): List<Match> = emptyList()

    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
