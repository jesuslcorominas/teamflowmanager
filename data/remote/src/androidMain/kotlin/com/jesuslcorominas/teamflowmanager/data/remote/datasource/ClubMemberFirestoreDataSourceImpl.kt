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
import kotlinx.coroutines.tasks.await

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
    override fun getClubMemberByUserId(userId: String): Flow<ClubMember?> =
        callbackFlow {
            if (userId.isEmpty()) {
                Log.w(TAG, "Empty user ID, cannot get club member")
                trySend(null)
                awaitClose { }
                return@callbackFlow
            }

            val listenerRegistration =
                firestore.collection(CLUB_MEMBERS_COLLECTION)
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
                            val modelWithId =
                                if (firestoreModel.id.isEmpty()) {
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

    override fun getClubMembers(clubFirestoreId: String): Flow<List<ClubMember>> =
        callbackFlow {
            require(clubFirestoreId.isNotBlank()) { "Club Firestore ID cannot be blank" }

            val listenerRegistration =
                firestore.collection(CLUB_MEMBERS_COLLECTION)
                    .whereEqualTo("clubId", clubFirestoreId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e(TAG, "Error getting club members from Firestore", error)
                            trySend(emptyList())
                            return@addSnapshotListener
                        }

                        if (snapshot == null || snapshot.isEmpty) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }

                        val clubMembers =
                            snapshot.documents.mapNotNull { document ->
                                val documentId = document.id
                                val firestoreModel = document.toObject(ClubMemberFirestoreModel::class.java)

                                firestoreModel?.let {
                                    // Ensure the id field is set from the document ID
                                    val modelWithId =
                                        if (it.id.isEmpty()) {
                                            it.copy(id = documentId)
                                        } else {
                                            it
                                        }
                                    modelWithId.toDomain()
                                }
                            }

                        Log.d(TAG, "Loaded ${clubMembers.size} club members for club: $clubFirestoreId")
                        trySend(clubMembers)
                    }

            awaitClose {
                listenerRegistration.remove()
            }
        }

    override suspend fun createOrUpdateClubMember(
        userId: String,
        name: String,
        email: String,
        clubId: Long,
        clubFirestoreId: String,
        roles: List<String>,
    ): ClubMember {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(clubFirestoreId.isNotBlank()) { "Club Firestore ID cannot be blank" }
        require(roles.isNotEmpty()) { "Roles cannot be empty" }

        try {
            // Use predictable ID format: userId_clubFirestoreId
            // This format is required by Firestore security rules
            val clubMemberId = "${userId}_$clubFirestoreId"
            val clubMemberDocRef = firestore.collection(CLUB_MEMBERS_COLLECTION).document(clubMemberId)

            // Create club member model
            val clubMemberModel =
                ClubMemberFirestoreModel(
                    id = clubMemberId,
                    userId = userId,
                    name = name,
                    email = email,
                    clubId = clubFirestoreId,
                    roles = roles,
                )

            // Create or update the club member document
            clubMemberDocRef.set(clubMemberModel).await()
            Log.d(TAG, "ClubMember created/updated for userId: $userId with roles: $roles in club: $clubFirestoreId")

            return clubMemberModel.toDomain()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating/updating club member in Firestore", e)
            throw e
        }
    }

    override suspend fun updateClubMemberRoles(
        userId: String,
        clubFirestoreId: String,
        roles: List<String>,
    ) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(clubFirestoreId.isNotBlank()) { "Club Firestore ID cannot be blank" }
        require(roles.isNotEmpty()) { "Roles cannot be empty" }

        try {
            // Use predictable ID format: userId_clubFirestoreId
            val clubMemberId = "${userId}_$clubFirestoreId"
            val updates =
                mapOf(
                    "roles" to roles,
                )

            firestore.collection(CLUB_MEMBERS_COLLECTION)
                .document(clubMemberId)
                .update(updates)
                .await()

            Log.d(TAG, "ClubMember roles updated for userId: $userId in club: $clubFirestoreId to roles: $roles")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating club member roles in Firestore", e)
            throw e
        }
    }

    override suspend fun addClubMemberRole(
        userId: String,
        clubFirestoreId: String,
        role: String,
    ) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(clubFirestoreId.isNotBlank()) { "Club Firestore ID cannot be blank" }
        require(role.isNotBlank()) { "Role cannot be blank" }

        try {
            // Use predictable ID format: userId_clubFirestoreId
            val clubMemberId = "${userId}_$clubFirestoreId"

            // Get current club member
            val document =
                firestore.collection(CLUB_MEMBERS_COLLECTION)
                    .document(clubMemberId)
                    .get()
                    .await()

            if (!document.exists()) {
                throw IllegalStateException("ClubMember not found for userId: $userId in club: $clubFirestoreId")
            }

            val firestoreModel =
                document.toObject(ClubMemberFirestoreModel::class.java)
                    ?: throw IllegalStateException("Failed to parse ClubMember document")

            // Add role if not already present
            val currentRoles = firestoreModel.roles.toMutableList()
            if (!currentRoles.contains(role)) {
                currentRoles.add(role)

                // Update the document
                val updates =
                    mapOf(
                        "roles" to currentRoles,
                    )

                firestore.collection(CLUB_MEMBERS_COLLECTION)
                    .document(clubMemberId)
                    .update(updates)
                    .await()

                Log.d(TAG, "Added role $role to ClubMember for userId: $userId in club: $clubFirestoreId")
            } else {
                Log.d(TAG, "Role $role already exists for userId: $userId in club: $clubFirestoreId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding role to club member in Firestore", e)
            throw e
        }
    }

    override suspend fun getClubMemberByUserIdAndClub(
        userId: String,
        clubFirestoreId: String,
    ): ClubMember? {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(clubFirestoreId.isNotBlank()) { "Club Firestore ID cannot be blank" }

        try {
            // Use predictable ID format: userId_clubFirestoreId
            val clubMemberId = "${userId}_$clubFirestoreId"
            val document =
                firestore.collection(CLUB_MEMBERS_COLLECTION)
                    .document(clubMemberId)
                    .get()
                    .await()

            if (!document.exists()) {
                Log.d(TAG, "ClubMember not found for userId: $userId in club: $clubFirestoreId")
                return null
            }

            val documentId = document.id
            val firestoreModel = document.toObject(ClubMemberFirestoreModel::class.java)

            return firestoreModel?.let {
                // Ensure the id field is set from the document ID
                val modelWithId =
                    if (it.id.isEmpty()) {
                        it.copy(id = documentId)
                    } else {
                        it
                    }
                modelWithId.toDomain()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting club member by user and club from Firestore", e)
            throw e
        }
    }
}
