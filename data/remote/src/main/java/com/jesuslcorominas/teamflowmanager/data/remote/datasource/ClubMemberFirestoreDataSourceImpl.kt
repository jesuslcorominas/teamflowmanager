package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubMemberDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.ClubMemberFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of ClubMemberDataSource.
 * This implementation manages club member data in Firebase Firestore.
 */
class ClubMemberFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : ClubMemberDataSource {

    companion object {
        private const val TAG = "ClubMemberFirestoreDataSource"
        private const val CLUB_MEMBERS_COLLECTION = "clubMembers"
    }

    override suspend fun createOrUpdateClubMember(clubMember: ClubMember) {
        try {
            val firestoreModel = clubMember.toFirestoreModel()
            
            // Get clubFirestoreId and validate
            val clubFirestoreId = validateClubFirestoreId(clubMember.firestoreId)
            
            // Query for existing member with same userId and clubId
            val existingMember = getClubMember(clubMember.userId, clubFirestoreId)
            
            if (existingMember != null) {
                // Update existing member
                val documentId = existingMember.firestoreId
                if (documentId.isNullOrBlank()) {
                    Log.e(TAG, "Cannot update club member without document ID")
                    throw IllegalStateException("Cannot update club member without document ID")
                }
                
                val modelWithClubId = firestoreModel.copy(
                    id = documentId,
                    clubId = clubFirestoreId
                )
                firestore.collection(CLUB_MEMBERS_COLLECTION)
                    .document(documentId)
                    .set(modelWithClubId)
                    .await()
                Log.d(TAG, "Club member updated successfully with id: $documentId")
            } else {
                // Create new member
                val docRef = firestore.collection(CLUB_MEMBERS_COLLECTION).document()
                
                val modelWithClubId = firestoreModel.copy(
                    id = docRef.id,
                    clubId = clubFirestoreId
                )
                docRef.set(modelWithClubId).await()
                Log.d(TAG, "Club member created successfully with id: ${docRef.id}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error creating/updating club member in Firestore", e)
            throw e
        }
    }

    override suspend fun getClubMember(userId: String, clubFirestoreId: String): ClubMember? {
        if (userId.isBlank() || clubFirestoreId.isBlank()) {
            Log.w(TAG, "Empty userId or clubFirestoreId provided")
            return null
        }

        try {
            val snapshot = firestore.collection(CLUB_MEMBERS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("clubId", clubFirestoreId)
                .limit(1)
                .get()
                .await()

            val document = snapshot.documents.firstOrNull()
            if (document == null) {
                Log.d(TAG, "No club member found for userId: $userId, clubId: $clubFirestoreId")
                return null
            }

            val documentId = document.id
            val firestoreModel = document.toObject(ClubMemberFirestoreModel::class.java)

            if (firestoreModel != null) {
                val modelWithId = if (firestoreModel.id.isEmpty()) {
                    firestoreModel.copy(id = documentId)
                } else {
                    firestoreModel
                }
                val clubMember = modelWithId.toDomain()
                Log.d(TAG, "Club member found with id: $documentId")
                return clubMember
            }
            return null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error getting club member from Firestore", e)
            throw e
        }
    }

    /**
     * Validates that clubFirestoreId is not null or blank.
     * @throws IllegalStateException if validation fails
     */
    private fun validateClubFirestoreId(clubFirestoreId: String?): String {
        if (clubFirestoreId.isNullOrBlank()) {
            Log.e(TAG, "Cannot create/update club member without club Firestore ID")
            throw IllegalStateException("Cannot create/update club member without club Firestore ID")
        }
        return clubFirestoreId
    }
}
