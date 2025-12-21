package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubMemberDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.ClubMemberFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Firestore-based implementation of ClubMemberDataSource.
 * This implementation retrieves club member data from Firebase Firestore.
 */
class ClubMemberFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : ClubMemberDataSource {

    companion object {
        private const val TAG = "ClubMemberFirestoreDS"
        private const val CLUB_MEMBERS_COLLECTION = "clubMembers"
    }

    /**
     * Gets the club member for a given user ID from Firestore.
     */
    override fun getClubMemberByUserId(userId: String): Flow<ClubMember?> = callbackFlow {
        if (userId.isEmpty()) {
            Log.w(TAG, "Empty user ID, cannot get club member")
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(CLUB_MEMBERS_COLLECTION)
            .whereEqualTo("userId", userId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting club member from Firestore", error)
                    trySend(null)
                    return@addSnapshotListener
                }

                val document = snapshot?.documents?.firstOrNull()
                if (document == null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                // Get the document ID explicitly to ensure it's available
                val documentId = document.id
                val firestoreModel = document.toObject(ClubMemberFirestoreModel::class.java)

                if (firestoreModel != null) {
                    // Ensure the id field is set from the document ID
                    // This is needed because @DocumentId may not always populate the field
                    // during snapshot listener callbacks (consistent with TeamFirestoreDataSourceImpl)
                    val modelWithId = if (firestoreModel.id.isEmpty()) {
                        firestoreModel.copy(id = documentId)
                    } else {
                        firestoreModel
                    }
                    val clubMember = modelWithId.toDomain()
                    Log.d(TAG, "ClubMember loaded for userId: $userId, clubId: ${clubMember.clubId}")
                    trySend(clubMember)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }
}
