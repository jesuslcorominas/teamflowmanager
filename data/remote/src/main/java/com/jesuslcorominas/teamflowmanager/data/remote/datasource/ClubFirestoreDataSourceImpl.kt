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
     * This operation creates the club first, then the clubMember, to work with Firestore security rules.
     * Note: We can't use batch writes here because the clubMember creation rule checks if the user
     * is the club owner, which requires the club document to exist first.
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

            // First, create the club document
            clubDocRef.set(clubModel).await()
            Log.d(TAG, "Club created successfully with id: $clubId, invitationCode: $invitationCode")

            // Create clubMember document reference with predictable ID format: userId_clubId
            // This format is required by Firestore security rules
            val clubMemberId = "${currentUserId}_${clubId}"
            val clubMemberDocRef = firestore.collection(CLUB_MEMBERS_COLLECTION).document(clubMemberId)

            // Create clubMember model for the president
            val clubMemberModel = ClubMemberFirestoreModel(
                id = clubMemberId,
                userId = currentUserId,
                name = currentUserName,
                email = currentUserEmail,
                clubId = clubId,
                role = ROLE_PRESIDENTE
            )

            // Then create the clubMember document (now the club exists and security rules can verify ownership)
            clubMemberDocRef.set(clubMemberModel).await()
            Log.d(TAG, "ClubMember created for userId: $currentUserId with role: $ROLE_PRESIDENTE")

            return clubModel.toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error creating club with owner in Firestore", e)
            throw e
        }
    }

    override suspend fun getClubByInvitationCode(invitationCode: String): Club? {
        require(invitationCode.isNotBlank()) { "Invitation code cannot be blank" }

        try {
            val querySnapshot = firestore.collection(CLUBS_COLLECTION)
                .whereEqualTo("invitationCode", invitationCode)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Log.d(TAG, "No club found with invitation code: $invitationCode")
                return null
            }

            val document = querySnapshot.documents.first()
            val documentId = document.id
            val firestoreModel = document.toObject(ClubFirestoreModel::class.java)

            if (firestoreModel == null) {
                Log.w(TAG, "Could not deserialize club document")
                return null
            }

            // Ensure the id field is set from the document ID
            val modelWithId = if (firestoreModel.id.isEmpty()) {
                firestoreModel.copy(id = documentId)
            } else {
                firestoreModel
            }

            Log.d(TAG, "Found club with invitation code: ${modelWithId.name} (id: ${modelWithId.id})")
            return modelWithId.toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error getting club by invitation code from Firestore", e)
            throw e
        }
    }
}
