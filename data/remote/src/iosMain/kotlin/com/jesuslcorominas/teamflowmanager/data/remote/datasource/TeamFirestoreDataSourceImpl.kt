package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.TeamFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class TeamFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : TeamDataSource {

    companion object {
        private const val TEAMS_COLLECTION = "teams"
    }

    private suspend fun currentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun getTeam(): Flow<Team?> = flow {
        val userId = currentUserId()
        if (userId == null) {
            emit(null)
            return@flow
        }
        val snapshots = firestore.collection(TEAMS_COLLECTION)
            .where { "assignedCoachId" equalTo userId }
            .limit(1)
            .snapshots
        emitAll(
            snapshots.map { qs ->
                qs.documents.firstOrNull()?.let { doc ->
                    try {
                        doc.data<TeamFirestoreModel>().copy(id = doc.id).toDomain()
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        )
    }

    override fun getTeamByCoachId(coachId: String): Flow<Team?> = flow {
        val snapshots = firestore.collection(TEAMS_COLLECTION)
            .where { "assignedCoachId" equalTo coachId }
            .limit(1)
            .snapshots
        emitAll(
            snapshots.map { qs ->
                qs.documents.firstOrNull()?.let { doc ->
                    try {
                        doc.data<TeamFirestoreModel>().copy(id = doc.id).toDomain()
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        )
    }

    override fun getTeamsByClub(clubFirestoreId: String): Flow<List<Team>> = flow {
        val snapshots = firestore.collection(TEAMS_COLLECTION)
            .where { "clubId" equalTo clubFirestoreId }
            .snapshots
        emitAll(
            snapshots.map { qs ->
                qs.documents.mapNotNull { doc ->
                    try {
                        doc.data<TeamFirestoreModel>().copy(id = doc.id).toDomain()
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        )
    }

    override suspend fun getTeamByFirestoreId(teamFirestoreId: String): Team? =
        try {
            val doc = firestore.collection(TEAMS_COLLECTION).document(teamFirestoreId).get()
            if (doc.exists) doc.data<TeamFirestoreModel>().copy(id = doc.id).toDomain() else null
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }

    override suspend fun getOrphanTeams(ownerId: String): List<Team> = emptyList()

    // Local-only operations — no-op for remote data source
    override suspend fun hasLocalTeamWithoutUserId(): Boolean = false
    override suspend fun getTeamDirect(): Team? = null
    override suspend fun clearLocalData() = Unit

    // Write operations — not implemented for iOS Phase 2 MVP
    override suspend fun insertTeam(team: Team) =
        throw NotImplementedError("insertTeam not implemented for iOS Phase 2")

    override suspend fun updateTeam(team: Team) =
        throw NotImplementedError("updateTeam not implemented for iOS Phase 2")

    override suspend fun updateTeamClubId(teamFirestoreId: String, clubId: Long, clubFirestoreId: String) =
        throw NotImplementedError("updateTeamClubId not implemented for iOS Phase 2")

    override suspend fun updateTeamCoachId(teamFirestoreId: String, coachId: String) =
        throw NotImplementedError("updateTeamCoachId not implemented for iOS Phase 2")
}
