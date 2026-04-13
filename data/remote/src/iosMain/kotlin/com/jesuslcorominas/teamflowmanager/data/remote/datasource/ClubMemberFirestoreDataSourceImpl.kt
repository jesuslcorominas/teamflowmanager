package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubMemberDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.ClubMemberFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class ClubMemberFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : ClubMemberDataSource {
    companion object {
        private const val CLUB_MEMBERS_COLLECTION = "clubMembers"
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

    // Write operations — not implemented for iOS Phase 2 MVP
    override suspend fun createOrUpdateClubMember(
        userId: String,
        name: String,
        email: String,
        clubNumericId: Long,
        clubId: String,
        roles: List<String>,
    ): ClubMember = throw NotImplementedError("createOrUpdateClubMember not implemented for iOS Phase 2")

    override suspend fun updateClubMemberRoles(
        userId: String,
        clubId: String,
        roles: List<String>,
    ) = throw NotImplementedError("updateClubMemberRoles not implemented for iOS Phase 2")

    override suspend fun addClubMemberRole(
        userId: String,
        clubId: String,
        role: String,
    ) = throw NotImplementedError("addClubMemberRole not implemented for iOS Phase 2")
}
