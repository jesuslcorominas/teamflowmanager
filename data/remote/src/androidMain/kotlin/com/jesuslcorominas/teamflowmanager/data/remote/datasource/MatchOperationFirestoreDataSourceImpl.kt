package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchOperationDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.MatchOperationFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
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
            val snapshot = firestore.collection(TEAMS_COLLECTION)
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
        val firestoreModel = operation.toFirestoreModel().copy(
            id = docRef.id,
            teamId = teamDocId
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
            val snapshot = firestore.collection(OPERATIONS_COLLECTION)
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
}
