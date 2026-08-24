package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.parseSubstitutionDocument
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.util.toLegacyId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of PlayerSubstitutionDataSource.
 */
class PlayerSubstitutionFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : PlayerSubstitutionDataSource {
    companion object {
        private const val SUBSTITUTIONS_COLLECTION = "substitutions"
        private const val TEAMS_COLLECTION = "teams"
        private const val MATCHES_COLLECTION = "matches"
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

    // For non-coach roles (e.g. president), derive teamId from the match document.
    private suspend fun getTeamDocumentIdOrFromMatch(matchId: String): String? =
        getTeamDocumentId()
            ?: try {
                firestore.collection(MATCHES_COLLECTION).document(matchId).get().await()
                    .getString("teamId")
            } catch (_: Exception) {
                null
            }

    override fun getMatchSubstitutions(matchId: String): Flow<List<PlayerSubstitution>> =
        callbackFlow {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            val teamDocId = getTeamDocumentIdOrFromMatch(matchId)
            if (teamDocId == null) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            // One-time fetch for legacy Long-ID docs (pre-migration).
            // TODO: remove after backward-compat window closes.
            val legacySubstitutions =
                try {
                    firestore.collection(SUBSTITUTIONS_COLLECTION)
                        .whereEqualTo("teamId", teamDocId)
                        .whereEqualTo("matchId", matchId.toLegacyId())
                        .get()
                        .await()
                        .documents.mapNotNull { document ->
                            parseSubstitutionDocument(document.data, document.id, matchId)
                        }
                } catch (_: Exception) {
                    emptyList()
                }

            // Real-time listener for new String-ID docs.
            val listenerRegistration =
                firestore.collection(SUBSTITUTIONS_COLLECTION)
                    .whereEqualTo("teamId", teamDocId)
                    .whereEqualTo("matchId", matchId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val newSubstitutions =
                            snapshot?.documents?.mapNotNull { document ->
                                parseSubstitutionDocument(document.data, document.id, matchId)
                            } ?: emptyList()
                        trySend(legacySubstitutions + newSubstitutions)
                    }

            awaitClose { listenerRegistration.remove() }
        }

    override suspend fun insertSubstitution(substitution: PlayerSubstitution): String {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to create a substitution")

        val docRef = firestore.collection(SUBSTITUTIONS_COLLECTION).document()
        val firestoreModel = substitution.toFirestoreModel().copy(id = docRef.id, teamId = teamDocId)

        try {
            docRef.set(firestoreModel).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getAllPlayerSubstitutionsDirect(): List<PlayerSubstitution> = emptyList()

    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
