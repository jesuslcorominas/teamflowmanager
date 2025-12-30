package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.TeamFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of TeamLocalDataSource.
 * This implementation stores team data in Firebase Firestore as a remote data source.
 * Team documents are stored in the "teams" collection with auto-generated document IDs.
 * The document ID is stored in the domain model's coachId field for reference during updates.
 * The ownerId field is set to the current authenticated user's ID as required by security rules.
 */
class TeamFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : TeamDataSource {

    companion object {
        private const val TAG = "TeamFirestoreDataSource"
        private const val TEAMS_COLLECTION = "teams"
    }

    /**
     * Gets the first team owned by the current user from Firestore.
     */
    override fun getTeam(): Flow<Team?> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "No authenticated user, cannot get team")
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(TEAMS_COLLECTION)
            .whereEqualTo("ownerId", currentUserId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting team from Firestore", error)
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
                val firestoreModel = document.toObject(TeamFirestoreModel::class.java)

                if (firestoreModel != null) {
                    // Ensure the id field is set from the document ID
                    val modelWithId = if (firestoreModel.id.isEmpty()) {
                        firestoreModel.copy(id = documentId)
                    } else {
                        firestoreModel
                    }
                    val team = modelWithId.toDomain()
                    Log.d(TAG, "Team loaded with documentId: $documentId, coachId: ${team.coachId}")
                    trySend(team)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun insertTeam(team: Team) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "No authenticated user, cannot insert team")
            throw IllegalStateException("User must be authenticated to create a team")
        }

        try {
            val firestoreModel = team.toFirestoreModel()
            // Use auto-generated document ID for new teams
            val docRef = firestore.collection(TEAMS_COLLECTION).document()
            // Set the ownerId to current user and document id
            val modelWithOwner = firestoreModel.copy(id = docRef.id, ownerId = currentUserId)
            docRef.set(modelWithOwner).await()
            Log.d(TAG, "Team inserted successfully with id: ${docRef.id}, ownerId: $currentUserId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting team to Firestore", e)
            throw e
        }
    }

    override suspend fun updateTeam(team: Team) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "No authenticated user, cannot update team")
            throw IllegalStateException("User must be authenticated to update a team")
        }

        try {
            val documentId = team.coachId
            if (documentId.isNullOrEmpty()) {
                Log.w(TAG, "Cannot update team without document ID (stored in coachId)")
                throw IllegalStateException("Cannot update team without document ID")
            }

            val firestoreModel = team.toFirestoreModel()
            // Ensure ownerId is set for security rules
            val modelWithOwner = firestoreModel.copy(ownerId = currentUserId)
            firestore.collection(TEAMS_COLLECTION)
                .document(documentId)
                .set(modelWithOwner)
                .await()
            Log.d(TAG, "Team updated successfully with id: $documentId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error updating team in Firestore", e)
            throw e
        }
    }

    override fun getTeamByCoachId(coachId: String): Flow<Team?> = callbackFlow {
        val listenerRegistration = firestore.collection(TEAMS_COLLECTION)
            .document(coachId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting team by coachId from Firestore", error)
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                // Get the document ID explicitly to ensure it's available
                val documentId = snapshot.id
                val firestoreModel = snapshot.toObject(TeamFirestoreModel::class.java)

                if (firestoreModel != null) {
                    // Ensure the id field is set from the document ID
                    val modelWithId = if (firestoreModel.id.isEmpty()) {
                        firestoreModel.copy(id = documentId)
                    } else {
                        firestoreModel
                    }
                    val team = modelWithId.toDomain()
                    Log.d(TAG, "Team loaded by coachId with documentId: $documentId, coachId: ${team.coachId}")
                    trySend(team)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * This method is not applicable for remote Firestore data source.
     * It's only relevant for local Room database.
     * @return false as remote storage always has userId association
     */
    override suspend fun hasLocalTeamWithoutUserId(): Boolean = false

    /**
     * This method is not applicable for remote Firestore data source.
     * @return null as direct access is not needed for remote storage
     */
    override suspend fun getTeamDirect(): Team? = null

    /**
     * This method is not applicable for remote Firestore data source.
     * Only relevant for local Room database cleanup.
     */
    override suspend fun clearLocalData() {
        // No-op for remote data source
    }

    override suspend fun getOrphanTeams(ownerId: String): List<Team> {
        require(ownerId.isNotBlank()) { "Owner ID cannot be blank" }

        try {
            // Get all teams for this owner
            // Note: We can't use whereEqualTo("clubId", null) because that only matches
            // documents with clubId explicitly set to null, not documents without the field
            val querySnapshot = firestore.collection(TEAMS_COLLECTION)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()

            // Filter to only include teams without a clubId field (orphan teams)
            val teams = querySnapshot.documents.mapNotNull { document ->
                val documentId = document.id
                
                // Check if document has clubId field and skip if it does
                val hasClubId = document.contains("clubId") && document.getString("clubId") != null
                if (hasClubId) {
                    return@mapNotNull null
                }
                
                val firestoreModel = document.toObject(TeamFirestoreModel::class.java)

                if (firestoreModel != null) {
                    // Ensure the id field is set from the document ID
                    val modelWithId = if (firestoreModel.id.isEmpty()) {
                        firestoreModel.copy(id = documentId)
                    } else {
                        firestoreModel
                    }
                    modelWithId.toDomain()
                } else {
                    null
                }
            }

            Log.d(TAG, "Found ${teams.size} orphan teams for user $ownerId")
            return teams
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error getting orphan teams from Firestore", e)
            throw e
        }
    }

    override suspend fun updateTeamClubId(teamCoachId: String, clubId: Long, clubFirestoreId: String) {
        require(teamCoachId.isNotBlank()) { "Team coach ID cannot be blank" }
        require(clubFirestoreId.isNotBlank()) { "Club Firestore ID cannot be blank" }

        try {
            val updates = mapOf(
                "clubId" to clubFirestoreId
            )

            // Use set with merge to ensure the operation succeeds even if the document
            // doesn't have all fields yet
            firestore.collection(TEAMS_COLLECTION)
                .document(teamCoachId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .await()

            Log.d(TAG, "Team $teamCoachId linked to club $clubFirestoreId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error updating team club ID in Firestore", e)
            throw e
        }
    }
}
