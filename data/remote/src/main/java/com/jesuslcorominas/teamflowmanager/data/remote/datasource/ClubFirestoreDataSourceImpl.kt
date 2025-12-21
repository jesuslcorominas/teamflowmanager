package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.ClubFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.ClubMemberFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.util.InvitationCodeGenerator
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of ClubDataSource.
 * This implementation stores club data in Firebase Firestore as a remote data source.
 * Club documents are stored in the "clubs" collection with auto-generated document IDs.
 * ClubMember documents are stored in the "clubMembers" collection.
 */
class ClubFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : ClubDataSource {

    companion object {
        private const val TAG = "ClubFirestoreDataSource"
        private const val CLUBS_COLLECTION = "clubs"
        private const val CLUB_MEMBERS_COLLECTION = "clubMembers"
        private const val ROLE_PRESIDENTE = "Presidente"
    }

    /**
     * Creates a new club with the current user as owner and president.
     * This operation is atomic using Firestore batch writes - both club and clubMember are created together.
     */
    override suspend fun createClubWithOwner(
        clubName: String,
        currentUserId: String,
        currentUserName: String,
        currentUserEmail: String
    ): Club {
        require(clubName.isNotBlank()) { "Club name cannot be blank" }
        require(currentUserId.isNotBlank()) { "User ID cannot be blank" }
        require(currentUserName.isNotBlank()) { "User name cannot be blank" }
        require(currentUserEmail.isNotBlank()) { "User email cannot be blank" }

        try {
            // Generate invitation code
            val invitationCode = InvitationCodeGenerator.generate()

            // Create club document reference
            val clubDocRef = firestore.collection(CLUBS_COLLECTION).document()
            val clubId = clubDocRef.id

            // Create club model
            val clubModel = ClubFirestoreModel(
                id = clubId,
                ownerId = currentUserId,
                name = clubName,
                invitationCode = invitationCode
            )

            // Create clubMember document reference
            val clubMemberDocRef = firestore.collection(CLUB_MEMBERS_COLLECTION).document()
            val clubMemberId = clubMemberDocRef.id

            // Create clubMember model for the president
            val clubMemberModel = ClubMemberFirestoreModel(
                id = clubMemberId,
                userId = currentUserId,
                name = currentUserName,
                email = currentUserEmail,
                clubId = clubId,
                role = ROLE_PRESIDENTE
            )

            // Use batch write to ensure atomicity
            val batch = firestore.batch()
            batch.set(clubDocRef, clubModel)
            batch.set(clubMemberDocRef, clubMemberModel)
            batch.commit().await()

            Log.d(TAG, "Club created successfully with id: $clubId, invitationCode: $invitationCode")
            Log.d(TAG, "ClubMember created for userId: $currentUserId with role: $ROLE_PRESIDENTE")

            return clubModel.toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error creating club with owner in Firestore", e)
            throw e
        }
    }
}
