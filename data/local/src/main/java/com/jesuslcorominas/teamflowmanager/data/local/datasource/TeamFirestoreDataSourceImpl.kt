package com.jesuslcorominas.teamflowmanager.data.local.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.firestore.TeamFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.local.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.local.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of TeamLocalDataSource.
 * This implementation stores team data in Firebase Firestore instead of local Room database.
 * Team documents are stored in the "teams" collection with auto-generated document IDs.
 * The document ID is stored in the domain model's coachId field for reference during updates.
 * The ownerId field is set to the current authenticated user's ID as required by security rules.
 */
class TeamFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : TeamLocalDataSource {

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

                val team = snapshot?.documents?.firstOrNull()
                    ?.toObject(TeamFirestoreModel::class.java)
                    ?.toDomain()
                trySend(team)
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

                val team = snapshot?.toObject(TeamFirestoreModel::class.java)?.toDomain()
                trySend(team)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }
}
