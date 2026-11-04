package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationCommit
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus

interface MatchOperationDataSource {
    /**
     * Creates a new operation in Firestore
     */
    suspend fun createOperation(operation: MatchOperation): String

    /**
     * Updates an existing operation
     */
    suspend fun updateOperation(operation: MatchOperation)

    /**
     * Gets an operation by its ID
     */
    suspend fun getOperationById(operationId: String): MatchOperation?

    /**
     * Atomically persists all data in [commit] in a single write batch.
     *
     * The batch includes player times, substitutions, the operation (marked COMPLETED),
     * and a partial field update of Match.lastCompletedOperationId.
     */
    suspend fun commitOperation(commit: MatchOperationCommit)

    /**
     * Returns all operations for [matchId] with the given [status].
     */
    suspend fun getOperationsByMatchAndStatus(
        matchId: String,
        status: MatchOperationStatus,
    ): List<MatchOperation>
}
