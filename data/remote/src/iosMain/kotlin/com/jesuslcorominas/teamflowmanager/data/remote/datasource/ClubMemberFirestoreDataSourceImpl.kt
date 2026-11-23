package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubMemberDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.ClubMemberFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class ClubMemberFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : ClubMemberDataSource {
    companion object {
        private const val CLUB_MEMBERS_COLLECTION = "clubMembers"
        private const val ROLES_FIELD = "roles"
    }

    override fun getClubMemberByUserId(userId: String): Flow<ClubMember?> =
        flow {
            val snapshots =
                firestore.collection(CLUB_MEMBERS_COLLECTION)
                    .where { "userId" equalTo userId }
                    .limit(1)
                    .snapshots
            emitAll(
                snapshots.map { qs ->
                    qs.documents.firstOrNull()?.let { doc ->
                        try {
                            doc.data<ClubMemberFirestoreModel>().copy(id = doc.id).toDomain()
                        } catch (_: Exception) {
                            null
                        }
                    }
                }.catch { e ->
                    // Like Android, a listener error degrades to "no member". Signing out makes
                    // Firestore end the open listeners with a permission error; rethrowing it
                    // reached the collecting coroutine uncaught and aborted the app (#526).
                    if (e is FirebaseFirestoreException) emit(null) else throw e
                },
            )
        }

    override fun getClubMembers(clubId: String): Flow<List<ClubMember>> =
        flow {
            val snapshots =
                firestore.collection(CLUB_MEMBERS_COLLECTION)
                    .where { "clubId" equalTo clubId }
                    .snapshots
            emitAll(
                snapshots.map { qs ->
                    qs.documents.mapNotNull { doc ->
                        try {
                            doc.data<ClubMemberFirestoreModel>().copy(id = doc.id).toDomain()
                        } catch (_: Exception) {
                            null
                        }
                    }
                }.catch { e ->
                    if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
                },
            )
        }

    override suspend fun getClubMemberByUserIdAndClub(
        userId: String,
        clubId: String,
    ): ClubMember? =
        try {
            val docId = "${userId}_$clubId"
            val doc = firestore.collection(CLUB_MEMBERS_COLLECTION).document(docId).get()
            if (doc.exists) doc.data<ClubMemberFirestoreModel>().copy(id = doc.id).toDomain() else null
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }

    // Write operations mirror the Android implementation. The document id is always
    // "userId_clubId": the Firestore security rules require that format.
    override suspend fun createOrUpdateClubMember(
        userId: String,
        name: String,
        email: String,
        clubId: String,
        roles: List<String>,
    ): ClubMember {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(clubId.isNotBlank()) { "Club ID cannot be blank" }
        require(roles.isNotEmpty()) { "Roles cannot be empty" }

        val clubMemberId = clubMemberId(userId, clubId)
        val model =
            ClubMemberFirestoreModel(
                id = clubMemberId,
                userId = userId,
                name = name,
                email = email,
                clubId = clubId,
                roles = roles,
            )
        firestore.collection(CLUB_MEMBERS_COLLECTION).document(clubMemberId).set(model)
        return model.toDomain()
    }

    override suspend fun updateClubMemberRoles(
        userId: String,
        clubId: String,
        roles: List<String>,
    ) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(clubId.isNotBlank()) { "Club ID cannot be blank" }
        require(roles.isNotEmpty()) { "Roles cannot be empty" }

        firestore.collection(CLUB_MEMBERS_COLLECTION)
            .document(clubMemberId(userId, clubId))
            .update(ROLES_FIELD to roles)
    }

    override suspend fun addClubMemberRole(
        userId: String,
        clubId: String,
        role: String,
    ) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(clubId.isNotBlank()) { "Club ID cannot be blank" }
        require(role.isNotBlank()) { "Role cannot be blank" }

        val docRef = firestore.collection(CLUB_MEMBERS_COLLECTION).document(clubMemberId(userId, clubId))
        val doc = docRef.get()
        check(doc.exists) { "ClubMember not found for userId: $userId in club: $clubId" }

        val currentRoles = doc.data<ClubMemberFirestoreModel>().roles
        if (role !in currentRoles) {
            docRef.update(ROLES_FIELD to currentRoles + role)
        }
    }

    private fun clubMemberId(
        userId: String,
        clubId: String,
    ): String = "${userId}_$clubId"

    override suspend fun removeClubMember(
        userId: String,
        clubId: String,
    ) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(clubId.isNotBlank()) { "Club ID cannot be blank" }
        val clubMemberId = "${userId}_$clubId"
        firestore.collection("clubMembers").document(clubMemberId).delete()
    }
}
