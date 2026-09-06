package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.ClubFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.ClubMemberFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.util.InvitationCodeGenerator
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.where

class ClubFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : ClubDataSource {
    companion object {
        private const val CLUBS_COLLECTION = "clubs"
        private const val CLUB_MEMBERS_COLLECTION = "clubMembers"
        private const val ROLE_PRESIDENTE = "Presidente"
        private const val NAME_FIELD = "name"
        private const val HOME_GROUND_FIELD = "homeGround"
        private const val INVITATION_CODE_FIELD = "invitationCode"
    }

    override suspend fun createClubWithOwner(
        clubName: String,
        currentUserId: String,
        currentUserName: String,
        currentUserEmail: String,
    ): Club {
        require(clubName.isNotBlank()) { "Club name cannot be blank" }
        require(currentUserId.isNotBlank()) { "User ID cannot be blank" }
        require(currentUserName.isNotBlank()) { "User name cannot be blank" }
        require(currentUserEmail.isNotBlank()) { "User email cannot be blank" }

        val invitationCode = InvitationCodeGenerator.generate()

        val clubDocRef = firestore.collection(CLUBS_COLLECTION).document
        val clubId = clubDocRef.id

        val clubModel =
            ClubFirestoreModel(
                id = clubId,
                ownerId = currentUserId,
                name = clubName,
                invitationCode = invitationCode,
            )

        clubDocRef.set(clubModel)

        val clubMemberId = "${currentUserId}_$clubId"
        val clubMemberModel =
            ClubMemberFirestoreModel(
                id = clubMemberId,
                userId = currentUserId,
                name = currentUserName,
                email = currentUserEmail,
                clubId = clubId,
                roles = listOf(ROLE_PRESIDENTE),
            )

        firestore.collection(CLUB_MEMBERS_COLLECTION).document(clubMemberId).set(clubMemberModel)

        return clubModel.toDomain()
    }

    override suspend fun getClubByInvitationCode(invitationCode: String): Club? {
        require(invitationCode.isNotBlank()) { "Invitation code cannot be blank" }
        return try {
            val snapshot =
                firestore.collection(CLUBS_COLLECTION)
                    .where { "invitationCode" equalTo invitationCode }
                    .limit(1)
                    .get()
            val doc = snapshot.documents.firstOrNull() ?: return null
            doc.data<ClubFirestoreModel>().copy(id = doc.id).toDomain()
        } catch (e: FirebaseFirestoreException) {
            null
        }
    }

    override suspend fun getClubById(id: String): Club? {
        require(id.isNotBlank()) { "ID cannot be blank" }
        return try {
            val doc = firestore.collection(CLUBS_COLLECTION).document(id).get()
            if (!doc.exists) return null
            doc.data<ClubFirestoreModel>().copy(id = doc.id).toDomain()
        } catch (e: FirebaseFirestoreException) {
            null
        }
    }

    override suspend fun regenerateInvitationCode(id: String): String {
        require(id.isNotBlank()) { "ID cannot be blank" }
        val newCode = InvitationCodeGenerator.generate()
        firestore.collection(CLUBS_COLLECTION).document(id)
            .update(INVITATION_CODE_FIELD to newCode)
        return newCode
    }

    override suspend fun updateClub(
        id: String,
        name: String,
        homeGround: String?,
    ): Club {
        require(id.isNotBlank()) { "ID cannot be blank" }
        require(name.isNotBlank()) { "Club name cannot be blank" }
        firestore.collection(CLUBS_COLLECTION).document(id)
            .update(mapOf(NAME_FIELD to name, HOME_GROUND_FIELD to homeGround))
        return getClubById(id)
            ?: throw IllegalStateException("Club not found after update: $id")
    }
}
