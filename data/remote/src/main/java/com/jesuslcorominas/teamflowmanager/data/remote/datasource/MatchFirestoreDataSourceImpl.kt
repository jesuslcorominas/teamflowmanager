package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.MatchFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of MatchDataSource.
 * This implementation stores match data in Firebase Firestore as a remote data source.
 * Match documents are stored in the "matches" collection with auto-generated document IDs.
 * The teamId field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
class MatchFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : MatchDataSource {

    companion object {
        private const val TAG = "MatchFirestoreDS"
        private const val MATCHES_COLLECTION = "matches"
        private const val TEAMS_COLLECTION = "teams"
    }

    /**
     * Converts a Firestore document to a domain Match object.
     * Ensures the id and teamId fields are properly set from document metadata.
     */
    private fun documentToMatch(
        documentId: String,
        firestoreModel: MatchFirestoreModel?,
        teamDocId: String,
    ): Match? {
        return firestoreModel?.let {
            // Always use the actual Firestore document ID and team ID
            // to ensure consistency, even if the model has these fields set
            val modelWithId = it.copy(id = documentId, teamId = teamDocId)
            modelWithId.toDomain()
        }
    }

    /**
     * Gets the team's Firestore document ID for the current authenticated user.
     * This is needed because security rules validate match access based on team ownership.
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
            Log.d(TAG, "getTeamDocumentId: Found teamDocId=$teamDocId (${snapshot.documents.size} documents)")
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
     * Gets a match by its ID as a real-time Flow.
     */
    override fun getMatchById(matchId: Long): Flow<Match?> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "getMatchById: No authenticated user, cannot get match (matchId=$matchId)")
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "getMatchById: No team found for user (matchId=$matchId)")
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        Log.d(TAG, "getMatchById: teamDocId=$teamDocId, matchId=$matchId")

        val listenerRegistration = firestore.collection(MATCHES_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getMatchById: Error from Firestore", error)
                    trySend(null)
                    return@addSnapshotListener
                }

                val allMatches = snapshot?.documents?.mapNotNull { document ->
                    documentToMatch(document.id, document.toObject(MatchFirestoreModel::class.java), teamDocId)
                } ?: emptyList()

                val match = allMatches.find { it.id == matchId }
                Log.d(TAG, "getMatchById: Found ${allMatches.size} matches, target matchId=$matchId found=${match != null}")

                trySend(match)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Gets all non-archived matches for the current user's team from Firestore as a real-time Flow.
     */
    override fun getAllMatches(): Flow<List<Match>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "No authenticated user, cannot get matches")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "No team found for user, cannot get matches")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(MATCHES_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("archived", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting matches from Firestore", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val matches = snapshot?.documents?.mapNotNull { document ->
                    documentToMatch(
                        document.id,
                        document.toObject(MatchFirestoreModel::class.java),
                        teamDocId
                    )
                } ?: emptyList()

                Log.d(TAG, "Loaded ${matches.size} matches for team: $teamDocId")
                trySend(matches)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Gets all archived matches for the current user's team from Firestore as a real-time Flow.
     */
    override fun getArchivedMatches(): Flow<List<Match>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "No authenticated user, cannot get archived matches")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "No team found for user, cannot get archived matches")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(MATCHES_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("archived", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting archived matches from Firestore", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val matches = snapshot?.documents?.mapNotNull { document ->
                    documentToMatch(
                        document.id,
                        document.toObject(MatchFirestoreModel::class.java),
                        teamDocId
                    )
                } ?: emptyList()

                Log.d(TAG, "Loaded ${matches.size} archived matches for team: $teamDocId")
                trySend(matches)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Gets all scheduled matches (non-archived with SCHEDULED status).
     */
    override suspend fun getScheduledMatches(): List<Match> {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "No team found, cannot get scheduled matches")
            return emptyList()
        }

        return try {
            val snapshot = firestore.collection(MATCHES_COLLECTION)
                .whereEqualTo("teamId", teamDocId)
                .whereEqualTo("archived", false)
                .whereEqualTo("status", MatchStatus.SCHEDULED.name)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                documentToMatch(
                    document.id,
                    document.toObject(MatchFirestoreModel::class.java),
                    teamDocId
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error getting scheduled matches from Firestore", e)
            emptyList()
        }
    }

    /**
     * Updates the captain for a specific match.
     */
    override suspend fun updateMatchCaptain(matchId: Long, captainId: Long?) {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "No team found, cannot update match captain - user may not be authenticated")
            return
        }

        val documentId = findDocumentIdByMatchId(teamDocId, matchId)
        if (documentId == null) {
            Log.w(TAG, "Cannot find match with id: $matchId to update captain")
            return
        }

        try {
            firestore.collection(MATCHES_COLLECTION)
                .document(documentId)
                .update("captainId", captainId ?: 0L)
                .await()
            Log.d(TAG, "Match captain updated: $documentId")
        } catch (e: CancellationException) {
            Log.w(TAG, "Match captain update was cancelled for id: $documentId")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error updating match captain: ${e.message}", e)
            // Don't rethrow - log the error but don't crash the app
        }
    }

    /**
     * Inserts a new match into Firestore.
     * Returns a stable Long ID derived from the Firestore document ID.
     */
    override suspend fun insertMatch(match: Match): Long {
        Log.d(TAG, "insertMatch: Starting insert for match opponent=${match.opponent}")

        val teamDocId = getTeamDocumentId()
        Log.d(TAG, "insertMatch: Got teamDocId=$teamDocId")

        if (teamDocId == null) {
            Log.e(TAG, "insertMatch: No team found, cannot insert match - user may not be authenticated")
            throw IllegalStateException("Team must exist to create a match")
        }

        val docRef = firestore.collection(MATCHES_COLLECTION).document()
        Log.d(TAG, "insertMatch: Created document reference with id=${docRef.id}")

        val firestoreModel = match.toFirestoreModel()
        val modelWithTeam = firestoreModel.copy(
            id = docRef.id,
            teamId = teamDocId,
        )

        Log.d(TAG, "insertMatch: Setting document in Firestore...")
        try {
            docRef.set(modelWithTeam).await()
            Log.d(TAG, "insertMatch: Match inserted successfully with id: ${docRef.id}, teamId: $teamDocId")
            return docRef.id.toStableId()
        } catch (e: CancellationException) {
            throw e
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            Log.e(TAG, "Firestore PERMISSION_DENIED or ERROR: ${e.code} - ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "General error inserting match: ${e.message}", e)
            throw e
        }
    }

    /**
     * Updates an existing match in Firestore.
     */
    override suspend fun updateMatch(match: Match) {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.e(TAG, "No team found, cannot update match - user may not be authenticated")
            throw IllegalStateException("Team must exist to update a match")
        }

        val documentId = findDocumentIdByMatchId(teamDocId, match.id)
        if (documentId == null) {
            Log.w(TAG, "Cannot find match with id: ${match.id} to update")
            throw IllegalStateException("Cannot update match without document ID")
        }

        val firestoreModel = match.toFirestoreModel()
        val modelWithTeam = firestoreModel.copy(
            id = documentId,
            teamId = teamDocId,
        )

        try {
            firestore.collection(MATCHES_COLLECTION)
                .document(documentId)
                .set(modelWithTeam)
                .await()
            Log.d(TAG, "Match updated successfully: $documentId")
        } catch (e: CancellationException) {
            Log.w(TAG, "Match update was cancelled for id: $documentId")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error updating match in Firestore: ${e.message}", e)
            throw e
        }
    }

    /**
     * Deletes a match from Firestore.
     */
    override suspend fun deleteMatch(matchId: Long) {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "No team found, cannot delete match - user may not be authenticated")
            return
        }

        try {
            val documentId = findDocumentIdByMatchId(teamDocId, matchId)
            if (documentId == null) {
                Log.w(TAG, "Cannot find match with id: $matchId to delete")
                return
            }

            firestore.collection(MATCHES_COLLECTION)
                .document(documentId)
                .delete()
                .await()
            Log.d(TAG, "Match deleted successfully: $documentId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting match from Firestore", e)
            // Don't rethrow - log the error but don't crash the app
        }
    }

    /**
     * Helper function to find the Firestore document ID for a match based on the Long match ID.
     * Note: This iterates through documents because we use a stable hash of the document ID as the Long ID.
     * This pattern is consistent with PlayerFirestoreDataSourceImpl.
     */
    private suspend fun findDocumentIdByMatchId(teamDocId: String, matchId: Long): String? {
        val snapshot = firestore.collection(MATCHES_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .get()
            .await()

        for (document in snapshot.documents) {
            val match = documentToMatch(document.id, document.toObject(MatchFirestoreModel::class.java), teamDocId)
            if (match?.id == matchId) {
                return document.id
            }
        }
        return null
    }

    /**
     * This method is not applicable for remote Firestore data source.
     * @return empty list as direct access is not needed for remote storage
     */
    override suspend fun getAllMatchesDirect(): List<Match> = emptyList()

    /**
     * This method is not applicable for remote Firestore data source.
     * Only relevant for local Room database cleanup.
     */
    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
