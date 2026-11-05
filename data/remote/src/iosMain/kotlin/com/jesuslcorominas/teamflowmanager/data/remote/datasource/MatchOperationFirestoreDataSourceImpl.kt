package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchOperationDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.MatchOperationFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toPeriodUpdateMap
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationCommit
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of MatchOperationDataSource for iOS (GitLive Firebase SDK).
 * Operations are stored in the "matchOperations" collection.
 */
class MatchOperationFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : MatchOperationDataSource {
    companion object {
        private const val OPERATIONS_COLLECTION = "matchOperations"
        private const val TEAMS_COLLECTION = "teams"
        private const val PLAYER_TIMES_COLLECTION = "playerTimes"
        private const val SUBSTITUTIONS_COLLECTION = "substitutions"
        private const val MATCHES_COLLECTION = "matches"
    }

    /**
     * Gets the team's Firestore document ID for the current authenticated user.
     * Uses assignedCoachId (same as Android) to find the team.
     */
    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return null
        return try {
            val snapshot =
                firestore.collection(TEAMS_COLLECTION)
                    .where { "assignedCoachId" equalTo currentUserId }
                    .limit(1)
                    .get()
            snapshot.documents.firstOrNull()?.id
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun createOperation(operation: MatchOperation): String {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to create operation")

        return try {
            // @Transient id is excluded from serialization, so add() auto-generates it
            val model = operation.toFirestoreModel().copy(teamId = teamDocId)
            val docRef = firestore.collection(OPERATIONS_COLLECTION).add(model)
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateOperation(operation: MatchOperation) {
        require(operation.id.isNotEmpty()) { "Operation ID cannot be empty" }

        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to update operation")

        try {
            val model =
                operation.toFirestoreModel().copy(
                    id = operation.id,
                    teamId = teamDocId,
                )
            firestore.collection(OPERATIONS_COLLECTION)
                .document(operation.id)
                .set(model)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getOperationById(operationId: String): MatchOperation? {
        return try {
            val doc = firestore.collection(OPERATIONS_COLLECTION).document(operationId).get()
            if (!doc.exists) return null
            doc.data<MatchOperationFirestoreModel>().copy(id = doc.id).toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun commitOperation(commit: MatchOperationCommit) {
        val teamDocId =
            getTeamDocumentId()
                ?: throw IllegalStateException("Team must exist to commit operation")

        try {
            val batch = firestore.batch()

            // 1. Write all PlayerTime records
            commit.playerTimes.forEach { playerTime ->
                val docId = "player_${playerTime.playerId}"
                val model = playerTime.toFirestoreModel().copy(teamId = teamDocId)
                val docRef = firestore.collection(PLAYER_TIMES_COLLECTION).document(docId)
                batch.set(docRef, model)
            }

            // 2. Write all substitutions
            commit.substitutions.forEach { substitution ->
                // gitlive exposes auto-id document creation as a property, not a function
                val subRef = firestore.collection(SUBSTITUTIONS_COLLECTION).document
                val model = substitution.toFirestoreModel().copy(id = subRef.id, teamId = teamDocId)
                batch.set(subRef, model)
            }

            // 3. Write the operation (already marked COMPLETED by the caller)
            val operationDocRef = firestore.collection(OPERATIONS_COLLECTION).document(commit.operation.id)
            val operationModel = commit.operation.toFirestoreModel().copy(teamId = teamDocId)
            batch.set(operationDocRef, operationModel)

            // 4. Partial field update on Match — lastCompletedOperationId plus, when present,
            //    the match state fields (status, periods, pauseCount). All written in the same
            //    batch so that either everything lands or nothing does (P1 fix).
            val matchDocRef = firestore.collection(MATCHES_COLLECTION).document(commit.matchId)
            val matchPairs =
                mutableListOf<Pair<String, Any>>(
                    "lastCompletedOperationId" to commit.lastCompletedOperationId,
                )
            commit.matchState?.let { state ->
                matchPairs.add("status" to state.status.name)
                matchPairs.add("periods" to state.periods.map { it.toPeriodUpdateMap() })
                state.pauseCount?.let { matchPairs.add("pauseCount" to it) }
            }
            batch.update(matchDocRef, *matchPairs.toTypedArray())

            batch.commit()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getOperationsByMatchAndStatus(
        matchId: String,
        status: MatchOperationStatus,
    ): List<MatchOperation> {
        return try {
            val snapshot =
                firestore.collection(OPERATIONS_COLLECTION)
                    .where { "matchId" equalTo matchId }
                    .where { "status" equalTo status.name }
                    .get()
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data<MatchOperationFirestoreModel>().copy(id = doc.id).toDomain()
                } catch (_: Exception) {
                    null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }
    }
}
