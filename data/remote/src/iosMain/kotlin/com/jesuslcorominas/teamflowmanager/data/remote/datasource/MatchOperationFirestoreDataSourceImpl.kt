package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchOperationDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.MatchOperationFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
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
}
