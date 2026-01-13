package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation

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
}
