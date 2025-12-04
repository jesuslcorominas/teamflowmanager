package com.jesuslcorominas.teamflowmanager.data.local.datasource

import android.util.Log
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
 * Team documents are stored in the "teams" collection with the coachId as the document ID.
 */
class TeamFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : TeamLocalDataSource {

    companion object {
        private const val TAG = "TeamFirestoreDataSource"
        private const val TEAMS_COLLECTION = "teams"
    }

    /**
     * Gets the first team from Firestore.
     * Note: This method is kept for backward compatibility.
     * Prefer using getTeamByCoachId for the new architecture.
     */
    override fun getTeam(): Flow<Team?> = callbackFlow {
        val listenerRegistration = firestore.collection(TEAMS_COLLECTION)
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
        try {
            val firestoreModel = team.toFirestoreModel()
            val documentId = team.coachId
            if (documentId.isNullOrEmpty()) {
                Log.w(TAG, "Cannot insert team without coachId")
                return
            }

            firestore.collection(TEAMS_COLLECTION)
                .document(documentId)
                .set(firestoreModel)
                .await()
            Log.d(TAG, "Team inserted successfully for coachId: $documentId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting team to Firestore", e)
        }
    }

    override suspend fun updateTeam(team: Team) {
        try {
            val firestoreModel = team.toFirestoreModel()
            val documentId = team.coachId
            if (documentId.isNullOrEmpty()) {
                Log.w(TAG, "Cannot update team without coachId")
                return
            }

            firestore.collection(TEAMS_COLLECTION)
                .document(documentId)
                .set(firestoreModel)
                .await()
            Log.d(TAG, "Team updated successfully for coachId: $documentId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error updating team in Firestore", e)
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
