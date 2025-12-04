package com.jesuslcorominas.teamflowmanager.data.local.datasource

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
            val documentId = team.coachId ?: return

            firestore.collection(TEAMS_COLLECTION)
                .document(documentId)
                .set(firestoreModel)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Silently fail for now - in production, consider logging or error handling
        }
    }

    override suspend fun updateTeam(team: Team) {
        try {
            val firestoreModel = team.toFirestoreModel()
            val documentId = team.coachId ?: return

            firestore.collection(TEAMS_COLLECTION)
                .document(documentId)
                .set(firestoreModel)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Silently fail for now - in production, consider logging or error handling
        }
    }

    override fun getTeamByCoachId(coachId: String): Flow<Team?> = callbackFlow {
        val listenerRegistration = firestore.collection(TEAMS_COLLECTION)
            .document(coachId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
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
