package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
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
) : MatchOperationDataSource {

    companion object {
        private const val TAG = "MatchOperationFirestoreDS"
        private const val OPERATIONS_COLLECTION = "matchOperations"
    }

    override suspend fun createOperation(operation: MatchOperation): String {
        Log.d(TAG, "createOperation: type=${operation.type}, matchId=${operation.matchId}")

        val docRef = firestore.collection(OPERATIONS_COLLECTION).document()
        val firestoreModel = operation.toFirestoreModel().copy(id = docRef.id)

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

        val firestoreModel = operation.toFirestoreModel()

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
