package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation

interface MatchOperationRepository {
    /**
     * Creates a new operation and returns its ID
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
}
