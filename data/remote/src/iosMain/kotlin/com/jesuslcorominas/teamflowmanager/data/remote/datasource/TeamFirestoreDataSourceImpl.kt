package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.TeamFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Team
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

class TeamFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : TeamDataSource {
    companion object {
        private const val TEAMS_COLLECTION = "teams"
        private const val CLUB_ID_FIELD = "clubId"
        private const val ASSIGNED_COACH_ID_FIELD = "assignedCoachId"
        private const val PENDING_COACH_EMAIL_FIELD = "pendingCoachEmail"
    }

    private suspend fun currentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun getTeam(): Flow<Team?> =
        flow {
            val userId = currentUserId()
            if (userId == null) {
                emit(null)
                return@flow
            }
            val snapshots =
                firestore.collection(TEAMS_COLLECTION)
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
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(null) else throw e
                },
            )
        }

    override fun getTeamByCoachId(coachId: String): Flow<Team?> =
        flow {
            val snapshots =
                firestore.collection(TEAMS_COLLECTION)
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
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(null) else throw e
                },
            )
        }

    override fun getTeamsByClub(clubId: String): Flow<List<Team>> =
        flow {
            val snapshots =
                firestore.collection(TEAMS_COLLECTION)
                    .where { "clubId" equalTo clubId }
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
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                },
            )
        }

    override suspend fun getTeamById(teamId: String): Team? =
        try {
            val doc = firestore.collection(TEAMS_COLLECTION).document(teamId).get()
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

    private suspend fun findTeamDocumentId(): String? {
        val userId = currentUserId() ?: return null
        return try {
            firestore.collection(TEAMS_COLLECTION)
                .where { "assignedCoachId" equalTo userId }
                .limit(1)
                .get()
                .documents
                .firstOrNull()
                ?.id
        } catch (_: Exception) {
            null
        }
    }

    // Write operations mirror the Android implementation: same collection, document ids and fields.
    override suspend fun insertTeam(team: Team) {
        checkNotNull(currentUserId()) { "User must be authenticated to create a team" }
        // Auto-generated document id. The model's id is @Transient, as Android's @DocumentId.
        firestore.collection(TEAMS_COLLECTION).document.set(team.toFirestoreModel())
    }

    override suspend fun updateTeam(team: Team) {
        val docId =
            team.id.takeIf { it.isNotBlank() } ?: findTeamDocumentId()
                ?: throw IllegalStateException("Cannot find team document to update")
        val model = team.toFirestoreModel()
        firestore.collection(TEAMS_COLLECTION).document(docId).set(model)
    }

    override suspend fun updateTeamClubId(
        teamId: String,
        clubId: String,
    ) {
        require(teamId.isNotBlank()) { "Team ID cannot be blank" }
        require(clubId.isNotBlank()) { "Club ID cannot be blank" }
        // Merge, as Android, so it succeeds even if the document does not have every field yet.
        firestore.collection(TEAMS_COLLECTION).document(teamId).set(mapOf(CLUB_ID_FIELD to clubId), merge = true)
    }

    override suspend fun updateTeamCoachId(
        teamId: String,
        coachId: String,
    ) {
        require(teamId.isNotBlank()) { "Team ID cannot be blank" }
        require(coachId.isNotBlank()) { "Coach ID cannot be blank" }
        firestore.collection(TEAMS_COLLECTION).document(teamId).update(ASSIGNED_COACH_ID_FIELD to coachId)
    }

    override suspend fun updateTeamPendingCoachEmail(
        teamId: String,
        email: String?,
    ) {
        require(teamId.isNotBlank()) { "Team ID cannot be blank" }
        firestore.collection(TEAMS_COLLECTION).document(teamId).update(PENDING_COACH_EMAIL_FIELD to email)
    }

    override suspend fun clearTeamCoach(teamId: String) {
        require(teamId.isNotBlank()) { "Team ID cannot be blank" }
        // Same field as Android: the document stores the coach as assignedCoachId. Writing "coachId"
        // left a stray field and, for a president, the security rules rejected the update.
        firestore.collection(TEAMS_COLLECTION).document(teamId).update(ASSIGNED_COACH_ID_FIELD to null)
    }
}
