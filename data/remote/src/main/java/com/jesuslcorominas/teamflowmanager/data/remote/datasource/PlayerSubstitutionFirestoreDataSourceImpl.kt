package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerSubstitutionFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of PlayerSubstitutionDataSource.
 * This implementation stores player substitution data in Firebase Firestore as a remote data source.
 * Substitution documents are stored in the "substitutions" collection with auto-generated document IDs.
 * The teamId field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
class PlayerSubstitutionFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : PlayerSubstitutionDataSource {

    companion object {
        private const val TAG = "SubstitutionFirestoreDS"
        private const val SUBSTITUTIONS_COLLECTION = "substitutions"
        private const val TEAMS_COLLECTION = "teams"
    }

    /**
     * Gets the team's Firestore document ID for the current authenticated user.
     * This is needed because security rules validate substitution access based on team ownership.
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
     * Gets all substitutions for a specific match as a real-time Flow.
     */
    override fun getMatchSubstitutions(matchId: Long): Flow<List<PlayerSubstitution>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "getMatchSubstitutions: No authenticated user (matchId=$matchId)")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "getMatchSubstitutions: No team found for user (matchId=$matchId)")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        Log.d(TAG, "getMatchSubstitutions: teamDocId=$teamDocId, matchId=$matchId")

        val listenerRegistration = firestore.collection(SUBSTITUTIONS_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("matchId", matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getMatchSubstitutions: Error from Firestore", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val substitutions = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PlayerSubstitutionFirestoreModel::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "getMatchSubstitutions: Loaded ${substitutions.size} substitutions for match $matchId")
                trySend(substitutions)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Inserts a new substitution into Firestore.
     * Returns a stable Long ID derived from the Firestore document ID.
     */
    override suspend fun insertSubstitution(substitution: PlayerSubstitution): Long {
        Log.d(TAG, "insertSubstitution: Starting insert for substitution matchId=${substitution.matchId}")

        val teamDocId = getTeamDocumentId()
        Log.d(TAG, "insertSubstitution: Got teamDocId=$teamDocId")

        if (teamDocId == null) {
            Log.e(TAG, "insertSubstitution: No team found, cannot insert substitution - user may not be authenticated")
            throw IllegalStateException("Team must exist to create a substitution")
        }

        val docRef = firestore.collection(SUBSTITUTIONS_COLLECTION).document()
        Log.d(TAG, "insertSubstitution: Created document reference with id=${docRef.id}")

        val firestoreModel = substitution.toFirestoreModel()
        val modelWithTeam = firestoreModel.copy(
            id = docRef.id,
            teamId = teamDocId,
        )

        Log.d(TAG, "insertSubstitution: Setting document in Firestore...")
        try {
            docRef.set(modelWithTeam).await()
            Log.d(TAG, "insertSubstitution: Substitution inserted successfully with id: ${docRef.id}, teamId: $teamDocId")
            return docRef.id.toStableId()
        } catch (e: CancellationException) {
            throw e
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            Log.e(TAG, "Firestore PERMISSION_DENIED or ERROR: ${e.code} - ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "General error inserting substitution: ${e.message}", e)
            throw e
        }
    }
}
