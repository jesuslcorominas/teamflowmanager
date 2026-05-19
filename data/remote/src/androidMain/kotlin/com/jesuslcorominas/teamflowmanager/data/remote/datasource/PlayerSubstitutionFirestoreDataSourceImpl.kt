package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerSubstitutionFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

// Pre-migration documents stored matchId/playerOutId/playerInId as Long (hash of Firestore doc ID).
private fun String.toLegacyId(): Long {
    var result = 0L
    var multiplier = 1L
    for (char in this) {
        result += char.code.toLong() * multiplier
        multiplier *= 31L
    }
    return kotlin.math.abs(result)
}

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

    override fun getMatchSubstitutions(matchId: String): Flow<List<PlayerSubstitution>> =
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
                firestore.collection(SUBSTITUTIONS_COLLECTION)
                    .whereEqualTo("teamId", teamDocId)
                    .whereIn("matchId", listOf(matchId, matchId.toLegacyId()))
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val substitutions =
                            snapshot?.documents?.mapNotNull { document ->
                                try {
                                    val rawData = document.data ?: return@mapNotNull null
                                    val rawPlayerOutId = rawData["playerOutId"]?.toString() ?: ""
                                    val rawPlayerInId = rawData["playerInId"]?.toString() ?: ""
                                    val model =
                                        try {
                                            document.toObject(
                                                PlayerSubstitutionFirestoreModel::class.java,
                                            ) ?: return@mapNotNull null
                                        } catch (_: Exception) {
                                            PlayerSubstitutionFirestoreModel(
                                                teamId = rawData["teamId"] as? String ?: teamDocId,
                                                matchId = matchId,
                                                playerOutId = rawPlayerOutId,
                                                playerInId = rawPlayerInId,
                                                substitutionTimeMillis =
                                                    rawData["substitutionTimeMillis"] as? Long ?: 0L,
                                                matchElapsedTimeMillis =
                                                    rawData["matchElapsedTimeMillis"] as? Long ?: 0L,
                                            )
                                        }
                                    model
                                        .copy(
                                            id = document.id,
                                            matchId = matchId,
                                            playerOutId = rawPlayerOutId,
                                            playerInId = rawPlayerInId,
                                        )
                                        .toDomain()
                                } catch (_: Exception) {
                                    null
                                }
                            } ?: emptyList()
                        trySend(substitutions)
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
