package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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
        private const val MATCHES_COLLECTION = "matches"
        private const val TEAMS_COLLECTION = "teams"
    }

    /**
     * Converts a Firestore document to a domain Match object.
     * Wraps toObject() in a try-catch so that legacy documents that have a
     * stored 'id' field (which conflicts with @DocumentId) are silently skipped
     * instead of crashing the app.
     */
    private fun documentToMatch(document: DocumentSnapshot, teamDocId: String): Match? {
        return try {
            val model = document.toObject(MatchFirestoreModel::class.java) ?: return null
            model.copy(id = document.id, teamId = teamDocId).toDomain()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Gets the team's Firestore document ID for the current authenticated user.
     * This is needed because security rules validate match access based on team ownership.
     */
    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId == null) {
            return null
        }

        return try {
            val snapshot = firestore.collection(TEAMS_COLLECTION)
                .whereEqualTo("assignedCoachId", currentUserId)
                .limit(1)
                .get()
                .await()

            val teamDocId = snapshot.documents.firstOrNull()?.id
            teamDocId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets a match by its ID as a real-time Flow.
     */
    override fun getMatchById(matchId: Long): Flow<Match?> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(MATCHES_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val allMatches = snapshot?.documents?.mapNotNull { document ->
                    documentToMatch(document, teamDocId)
                } ?: emptyList()

                val match = allMatches.find { it.id == matchId }

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
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(MATCHES_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("archived", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val matches = snapshot?.documents?.mapNotNull { document ->
                    documentToMatch(document, teamDocId)
                } ?: emptyList()

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
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(MATCHES_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("archived", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val matches = snapshot?.documents?.mapNotNull { document ->
                    documentToMatch(document, teamDocId)
                } ?: emptyList()

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
                documentToMatch(document, teamDocId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Updates the captain for a specific match.
     */
    override suspend fun updateMatchCaptain(matchId: Long, captainId: Long?) {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            return
        }

        val documentId = findDocumentIdByMatchId(teamDocId, matchId)
        if (documentId == null) {
            return
        }

        try {
            firestore.collection(MATCHES_COLLECTION)
                .document(documentId)
                .update("captainId", captainId ?: 0L)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Don't rethrow - log the error but don't crash the app
        }
    }

    /**
     * Inserts a new match into Firestore.
     * Returns a stable Long ID derived from the Firestore document ID.
     */
    override suspend fun insertMatch(match: Match): Long {
        val teamDocId = getTeamDocumentId()

        if (teamDocId == null) {
            throw IllegalStateException("Team must exist to create a match")
        }

        val docRef = firestore.collection(MATCHES_COLLECTION).document()

        val firestoreModel = match.toFirestoreModel()
        val modelWithTeam = firestoreModel.copy(
            id = docRef.id,
            teamId = teamDocId,
        )

        try {
            docRef.set(modelWithTeam).await()
            return docRef.id.toStableId()
        } catch (e: CancellationException) {
            throw e
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Updates an existing match in Firestore.
     */
    override suspend fun updateMatch(match: Match) {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            throw IllegalStateException("Team must exist to update a match")
        }

        val documentId = findDocumentIdByMatchId(teamDocId, match.id)
        if (documentId == null) {
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Deletes a match from Firestore.
     */
    override suspend fun deleteMatch(matchId: Long) {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            return
        }

        try {
            val documentId = findDocumentIdByMatchId(teamDocId, matchId)
            if (documentId == null) {
                return
            }

            firestore.collection(MATCHES_COLLECTION)
                .document(documentId)
                .delete()
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
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
            val match = documentToMatch(document, teamDocId)
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
