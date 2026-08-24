package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.MatchFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of MatchDataSource.
 */
class MatchFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : MatchDataSource {
    companion object {
        private const val MATCHES_COLLECTION = "matches"
        private const val TEAMS_COLLECTION = "teams"
    }

    private fun documentToMatch(
        document: DocumentSnapshot,
        teamDocId: String,
    ): Match? {
        return try {
            val rawData = document.data ?: return null

            // Pre-migration documents store captainId as Long and squadCallUpIds /
            // startingLineupIds as List<Long>. Firestore's class mapper throws a
            // ClassCastException when it encounters a Long value for a String field.
            // Read these fields directly from the raw map first so we can safely override
            // them after calling toObject().
            val squadCallUpIds =
                (rawData["squadCallUpIds"] as? List<*>)?.filterIsInstance<String>()
                    ?: emptyList()
            val startingLineupIds =
                (rawData["startingLineupIds"] as? List<*>)?.filterIsInstance<String>()
                    ?: emptyList()
            val captainId = rawData["captainId"] as? String ?: ""

            // Try the fast path: new documents (post-migration) deserialize cleanly.
            val model =
                try {
                    document.toObject(MatchFirestoreModel::class.java) ?: return null
                } catch (_: Exception) {
                    // Slow path: pre-migration document has Long values in String fields.
                    // Build the model manually from the raw map.
                    MatchFirestoreModel(
                        teamId = rawData["teamId"] as? String ?: teamDocId,
                        teamName = rawData["teamName"] as? String ?: "",
                        opponent = rawData["opponent"] as? String ?: "",
                        location = rawData["location"] as? String ?: "",
                        dateTime = rawData["dateTime"] as? Long,
                        numberOfPeriods = (rawData["numberOfPeriods"] as? Long)?.toInt() ?: 2,
                        squadCallUpIds = squadCallUpIds,
                        captainId = captainId,
                        startingLineupIds = startingLineupIds,
                        status = rawData["status"] as? String ?: MatchStatus.SCHEDULED.name,
                        archived = rawData["archived"] as? Boolean ?: false,
                        pauseCount = (rawData["pauseCount"] as? Long)?.toInt() ?: 0,
                        goals = (rawData["goals"] as? Long)?.toInt() ?: 0,
                        opponentGoals = (rawData["opponentGoals"] as? Long)?.toInt() ?: 0,
                        timeoutStartTimeMillis = rawData["timeoutStartTimeMillis"] as? Long ?: 0L,
                        periods = emptyList(),
                        lastCompletedOperationId = rawData["lastCompletedOperationId"] as? String,
                    )
                }

            model.copy(
                id = document.id,
                teamId = teamDocId,
                squadCallUpIds = squadCallUpIds,
                startingLineupIds = startingLineupIds,
                captainId = captainId,
            ).toDomain()
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return null
        return try {
            val snapshot =
                firestore.collection(TEAMS_COLLECTION)
                    .whereEqualTo("assignedCoachId", currentUserId)
                    .limit(1)
                    .get()
                    .await()
            snapshot.documents.firstOrNull()?.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    override fun getMatchById(matchId: String): Flow<Match?> =
        callbackFlow {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                trySend(null)
                awaitClose { }
                return@callbackFlow
            }

            val listenerRegistration =
                firestore.collection(MATCHES_COLLECTION)
                    .document(matchId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null || snapshot == null) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        if (!snapshot.exists()) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        val teamDocId = snapshot.getString("teamId") ?: ""
                        trySend(documentToMatch(snapshot, teamDocId))
                    }

            awaitClose { listenerRegistration.remove() }
        }

    override fun getMatchesByTeam(teamId: String): Flow<List<Match>> =
        callbackFlow {
            val listenerRegistration =
                firestore.collection(MATCHES_COLLECTION)
                    .whereEqualTo("teamId", teamId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val matches =
                            snapshot?.documents?.mapNotNull { document ->
                                documentToMatch(document, teamId)
                            } ?: emptyList()
                        trySend(matches)
                    }
            awaitClose { listenerRegistration.remove() }
        }

    override fun getAllMatches(): Flow<List<Match>> =
        callbackFlow {
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

            val listenerRegistration =
                firestore.collection(MATCHES_COLLECTION)
                    .whereEqualTo("teamId", teamDocId)
                    .whereEqualTo("archived", false)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val matches =
                            snapshot?.documents?.mapNotNull { document ->
                                documentToMatch(document, teamDocId)
                            } ?: emptyList()
                        trySend(matches)
                    }

            awaitClose { listenerRegistration.remove() }
        }

    override fun getArchivedMatches(): Flow<List<Match>> =
        callbackFlow {
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

            val listenerRegistration =
                firestore.collection(MATCHES_COLLECTION)
                    .whereEqualTo("teamId", teamDocId)
                    .whereEqualTo("archived", true)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val matches =
                            snapshot?.documents?.mapNotNull { document ->
                                documentToMatch(document, teamDocId)
                            } ?: emptyList()
                        trySend(matches)
                    }

            awaitClose { listenerRegistration.remove() }
        }

    override suspend fun getScheduledMatches(): List<Match> {
        val teamDocId = getTeamDocumentId() ?: return emptyList()
        return try {
            val snapshot =
                firestore.collection(MATCHES_COLLECTION)
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

    override suspend fun updateMatchCaptain(
        matchId: String,
        captainId: String?,
    ) {
        try {
            firestore.collection(MATCHES_COLLECTION)
                .document(matchId)
                .update("captainId", captainId ?: "")
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Don't rethrow
        }
    }

    override suspend fun insertMatch(match: Match): String {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to create a match")

        val docRef = firestore.collection(MATCHES_COLLECTION).document()
        val firestoreModel = match.toFirestoreModel().copy(id = docRef.id, teamId = teamDocId)

        try {
            docRef.set(firestoreModel).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateMatch(match: Match) {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to update a match")

        val firestoreModel = match.toFirestoreModel().copy(id = match.id, teamId = teamDocId)

        try {
            firestore.collection(MATCHES_COLLECTION)
                .document(match.id)
                .set(firestoreModel)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deleteMatch(matchId: String) {
        try {
            firestore.collection(MATCHES_COLLECTION)
                .document(matchId)
                .delete()
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Don't rethrow
        }
    }

    override suspend fun getAllMatchesDirect(): List<Match> = emptyList()

    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
