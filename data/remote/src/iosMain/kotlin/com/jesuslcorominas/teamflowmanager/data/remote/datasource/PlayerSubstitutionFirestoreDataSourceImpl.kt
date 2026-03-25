package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerSubstitutionFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
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

class PlayerSubstitutionFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : PlayerSubstitutionDataSource {
    companion object {
        private const val SUBSTITUTIONS_COLLECTION = "substitutions"
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

    override fun getMatchSubstitutions(matchId: Long): Flow<List<PlayerSubstitution>> =
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
                firestore.collection(SUBSTITUTIONS_COLLECTION)
                    .where { "teamId" equalTo teamDocId }
                    .where { "matchId" equalTo matchId }
                    .snapshots
            emitAll(
                snapshots.map { qs ->
                    qs.documents.mapNotNull { doc ->
                        try {
                            doc.data<PlayerSubstitutionFirestoreModel>().copy(id = doc.id).toDomain()
                        } catch (_: Exception) {
                            null
                        }
                    }
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                },
            )
        }

    override suspend fun insertSubstitution(substitution: PlayerSubstitution): Long {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to insert substitution")
        return try {
            val model = substitution.toFirestoreModel().copy(teamId = teamDocId)
            val docRef = firestore.collection(SUBSTITUTIONS_COLLECTION).add(model)
            docRef.id.toStableId()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getAllPlayerSubstitutionsDirect(): List<PlayerSubstitution> = emptyList()

    override suspend fun clearLocalData() = Unit
}
