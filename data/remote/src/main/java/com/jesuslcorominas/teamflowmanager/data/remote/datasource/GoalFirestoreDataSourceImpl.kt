package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.GoalFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of GoalDataSource.
 * This implementation stores goal data in Firebase Firestore as a remote data source.
 * Goal documents are stored in the "goals" collection with auto-generated document IDs.
 * The teamId field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
class GoalFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : GoalDataSource {

    companion object {
        private const val TAG = "GoalFirestoreDS"
        private const val GOALS_COLLECTION = "goals"
        private const val TEAMS_COLLECTION = "teams"
        private const val MATCHES_COLLECTION = "matches"
    }

    /**
     * Gets the team's Firestore document ID for the current authenticated user.
     * This is needed because security rules validate goal access based on team ownership.
     */
    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid
        Log.d(TAG, "getTeamDocumentId: currentUserId=$currentUserId")

        if (currentUserId == null) {
            Log.w(TAG, "getTeamDocumentId: No authenticated user")
            return null
        }

        return try {
            Log.d(TAG, "getTeamDocumentId: Querying Firestore for team with ownerId=$currentUserId")
            val snapshot = firestore.collection(TEAMS_COLLECTION)
                .whereEqualTo("ownerId", currentUserId)
                .limit(1)
                .get()
                .await()

            val teamDocId = snapshot.documents.firstOrNull()?.id
            Log.d(TAG, "getTeamDocumentId: Found teamDocId=$teamDocId")
            teamDocId
        } catch (e: CancellationException) {
            Log.w(TAG, "getTeamDocumentId: Query was cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getTeamDocumentId: Error getting team document ID", e)
            null
        }
    }

    /**
     * Helper function to find the Firestore document ID for a match based on the Long match ID.
     */
    private suspend fun findMatchDocumentId(teamDocId: String, matchId: Long): String? {
        return try {
            val snapshot = firestore.collection(MATCHES_COLLECTION)
                .whereEqualTo("teamId", teamDocId)
                .get()
                .await()

            for (document in snapshot.documents) {
                // Check if this match's stable ID matches
                val docId = document.id
                if (docId.toStableId() == matchId) {
                    Log.d(TAG, "findMatchDocumentId: Found match document ID: $docId for matchId: $matchId")
                    return docId
                }
            }
            Log.w(TAG, "findMatchDocumentId: No match found for matchId: $matchId")
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "findMatchDocumentId: Error finding match document ID", e)
            null
        }
    }

    /**
     * Gets all goals for a specific match as a real-time Flow.
     */
    override fun getMatchGoals(matchId: Long): Flow<List<Goal>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "getMatchGoals: No authenticated user (matchId=$matchId)")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "getMatchGoals: No team found for user (matchId=$matchId)")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        Log.d(TAG, "getMatchGoals: teamDocId=$teamDocId, matchId=$matchId")

        val listenerRegistration = firestore.collection(GOALS_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("matchId", matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getMatchGoals: Error from Firestore", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(GoalFirestoreModel::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "getMatchGoals: Loaded ${goals.size} goals for match $matchId")
                trySend(goals)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Gets all goals for the current user's team from Firestore as a real-time Flow.
     */
    override fun getAllTeamGoals(): Flow<List<Goal>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "getAllTeamGoals: No authenticated user")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "getAllTeamGoals: No team found for user")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        Log.d(TAG, "getAllTeamGoals: teamDocId=$teamDocId")

        val listenerRegistration = firestore.collection(GOALS_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getAllTeamGoals: Error from Firestore", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(GoalFirestoreModel::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "getAllTeamGoals: Loaded ${goals.size} goals for team $teamDocId")
                trySend(goals)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Inserts a new goal into Firestore.
     * Returns a stable Long ID derived from the Firestore document ID.
     */
    override suspend fun insertGoal(goal: Goal): Long {
        Log.d(TAG, "insertGoal: Starting insert for goal matchId=${goal.matchId}")

        val teamDocId = getTeamDocumentId()
        Log.d(TAG, "insertGoal: Got teamDocId=$teamDocId")

        if (teamDocId == null) {
            Log.e(TAG, "insertGoal: No team found, cannot insert goal - user may not be authenticated")
            throw IllegalStateException("Team must exist to create a goal")
        }

        // Find the match document ID for security rules
        // If we can't find it, use empty string and rely on teamId validation in security rules
        val matchDocId = findMatchDocumentId(teamDocId, goal.matchId)
        if (matchDocId == null) {
            Log.w(TAG, "insertGoal: No match document found for matchId=${goal.matchId}, continuing with empty matchDocId")
        }

        val docRef = firestore.collection(GOALS_COLLECTION).document()
        Log.d(TAG, "insertGoal: Created document reference with id=${docRef.id}")

        val firestoreModel = goal.toFirestoreModel()
        val modelWithTeam = firestoreModel.copy(
            id = docRef.id,
            teamId = teamDocId,
            matchDocId = matchDocId ?: "",
        )

        Log.d(TAG, "insertGoal: Setting document in Firestore...")
        try {
            docRef.set(modelWithTeam).await()
            Log.d(TAG, "insertGoal: Goal inserted successfully with id: ${docRef.id}, teamId: $teamDocId, matchDocId: ${matchDocId ?: "empty"}")
            return docRef.id.toStableId()
        } catch (e: CancellationException) {
            throw e
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            Log.e(TAG, "Firestore PERMISSION_DENIED or ERROR: ${e.code} - ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "General error inserting goal: ${e.message}", e)
            throw e
        }
    }
}
