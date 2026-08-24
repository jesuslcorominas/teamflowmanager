package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.parseSubstitutionDocument
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.util.toLegacyId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
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

class PlayerSubstitutionFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : PlayerSubstitutionDataSource {
    companion object {
        private const val SUBSTITUTIONS_COLLECTION = "substitutions"
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

    override fun getMatchSubstitutions(matchId: String): Flow<List<PlayerSubstitution>> =
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
            val newSubs =
                firestore.collection(SUBSTITUTIONS_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .where { "matchId" equalTo matchId }
                    .snapshots
                    .map { qs ->
                        qs.documents.mapNotNull { doc ->
                            parseSubstitutionDocument(doc.data<Map<String, Any?>>(), doc.id, teamDocId, matchId)
                        }
                    }.catch { e ->
                        if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                    }
            val legacySubs =
                firestore.collection(SUBSTITUTIONS_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .where { "matchId" equalTo matchId.toLegacyId() }
                    .snapshots
                    .map { qs ->
                        qs.documents.mapNotNull { doc ->
                            parseSubstitutionDocument(doc.data<Map<String, Any?>>(), doc.id, teamDocId, matchId)
                        }
                    }.catch { e ->
                        if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                    }
            emitAll(combine(newSubs, legacySubs) { a, b -> a + b })
        }

    override suspend fun insertSubstitution(substitution: PlayerSubstitution): String {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to insert substitution")
        return try {
            val model = substitution.toFirestoreModel().copy(teamId = teamDocId)
            val docRef = firestore.collection(SUBSTITUTIONS_COLLECTION).add(model)
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getAllPlayerSubstitutionsDirect(): List<PlayerSubstitution> = emptyList()

    override suspend fun clearLocalData() = Unit
}
