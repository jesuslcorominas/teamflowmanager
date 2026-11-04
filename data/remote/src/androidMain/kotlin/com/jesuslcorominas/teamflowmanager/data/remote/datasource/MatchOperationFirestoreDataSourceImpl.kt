package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchOperationDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.MatchOperationFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationCommit
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of MatchOperationDataSource.
 * Operations are stored in the "matchOperations" collection.
 */
class MatchOperationFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : MatchOperationDataSource {
    companion object {
        private const val TAG = "MatchOperationFirestoreDS"
        private const val OPERATIONS_COLLECTION = "matchOperations"
        private const val TEAMS_COLLECTION = "teams"
        private const val PLAYER_TIMES_COLLECTION = "playerTimes"
        private const val SUBSTITUTIONS_COLLECTION = "substitutions"
        private const val MATCHES_COLLECTION = "matches"
    }

    /**
     * Gets the team's Firestore document ID for the current authenticated user.
     */
    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "getTeamDocumentId: No authenticated user")
            return null
        }

        return try {
            val snapshot =
                firestore.collection(TEAMS_COLLECTION)
                    .whereEqualTo("assignedCoachId", currentUserId)
                    .limit(1)
                    .get()
                    .await()

            val teamDocId = snapshot.documents.firstOrNull()?.id
            Log.d(TAG, "getTeamDocumentId: Found teamDocId=$teamDocId")
            teamDocId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getTeamDocumentId: Error getting team document ID", e)
            null
        }
    }

    override suspend fun createOperation(operation: MatchOperation): String {
        Log.d(TAG, "createOperation: type=${operation.type}, matchId=${operation.matchId}")

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.e(TAG, "createOperation: No team found, cannot create operation")
            throw IllegalStateException("Team must exist to create operation")
        }

        val docRef = firestore.collection(OPERATIONS_COLLECTION).document()
        val firestoreModel =
            operation.toFirestoreModel().copy(
                id = docRef.id,
                teamId = teamDocId,
            )

        return try {
            docRef.set(firestoreModel).await()
            Log.d(TAG, "createOperation: Operation created successfully with id: ${docRef.id}")
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "createOperation: Error creating operation", e)
            throw e
        }
    }

    override suspend fun updateOperation(operation: MatchOperation) {
        Log.d(TAG, "updateOperation: id=${operation.id}, status=${operation.status}")

        if (operation.id.isEmpty()) {
            Log.e(TAG, "updateOperation: Operation ID is empty")
            throw IllegalArgumentException("Operation ID cannot be empty")
        }

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.e(TAG, "updateOperation: No team found, cannot update operation")
            throw IllegalStateException("Team must exist to update operation")
        }

        val firestoreModel = operation.toFirestoreModel().copy(teamId = teamDocId)

        try {
            firestore.collection(OPERATIONS_COLLECTION)
                .document(operation.id)
                .set(firestoreModel)
                .await()
            Log.d(TAG, "updateOperation: Operation updated successfully: ${operation.id}")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "updateOperation: Error updating operation", e)
            throw e
        }
    }

    override suspend fun getOperationById(operationId: String): MatchOperation? {
        Log.d(TAG, "getOperationById: operationId=$operationId")

        return try {
            val snapshot =
                firestore.collection(OPERATIONS_COLLECTION)
                    .document(operationId)
                    .get()
                    .await()

            val firestoreModel = snapshot.toObject(MatchOperationFirestoreModel::class.java)
            val operation = firestoreModel?.toDomain()
            Log.d(TAG, "getOperationById: Found operation: ${operation != null}")
            operation
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getOperationById: Error getting operation", e)
            null
        }
    }

    override suspend fun commitOperation(commit: MatchOperationCommit) {
        Log.d(TAG, "commitOperation: matchId=${commit.matchId}, operationId=${commit.operation.id}")

        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.e(TAG, "commitOperation: No team found, cannot commit operation")
            throw IllegalStateException("Team must exist to commit operation")
        }

        try {
            val batch = firestore.batch()

            // 1. Write all PlayerTime records
            commit.playerTimes.forEach { playerTime ->
                val docId = "player_${playerTime.playerId}"
                val model = playerTime.toFirestoreModel().copy(id = docId, teamId = teamDocId)
                val docRef = firestore.collection(PLAYER_TIMES_COLLECTION).document(docId)
                batch.set(docRef, model)
            }

            // 2. Write all substitutions
            commit.substitutions.forEach { substitution ->
                val subRef = firestore.collection(SUBSTITUTIONS_COLLECTION).document()
                val model = substitution.toFirestoreModel().copy(id = subRef.id, teamId = teamDocId)
                batch.set(subRef, model)
            }

            // 3. Write the operation (already marked COMPLETED by the caller)
            val operationDocRef = firestore.collection(OPERATIONS_COLLECTION).document(commit.operation.id)
            val operationModel = commit.operation.toFirestoreModel().copy(teamId = teamDocId)
            batch.set(operationDocRef, operationModel)

            // 4. Partial field update on Match — only lastCompletedOperationId, never the full document
            val matchDocRef = firestore.collection(MATCHES_COLLECTION).document(commit.matchId)
            batch.update(matchDocRef, "lastCompletedOperationId", commit.lastCompletedOperationId)

            batch.commit().await()
            Log.d(TAG, "commitOperation: Batch committed successfully for operationId=${commit.operation.id}")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "commitOperation: Error committing operation batch", e)
            throw e
        }
    }

    override suspend fun getOperationsByMatchAndStatus(
        matchId: String,
        status: MatchOperationStatus,
    ): List<MatchOperation> {
        Log.d(TAG, "getOperationsByMatchAndStatus: matchId=$matchId, status=$status")

        return try {
            val snapshot =
                firestore.collection(OPERATIONS_COLLECTION)
                    .whereEqualTo("matchId", matchId)
                    .whereEqualTo("status", status.name)
                    .get()
                    .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(MatchOperationFirestoreModel::class.java)?.toDomain()
                } catch (_: Exception) {
                    null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getOperationsByMatchAndStatus: Error querying operations", e)
            emptyList()
        }
    }
}
