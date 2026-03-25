package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchOperationDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository

internal class MatchOperationRepositoryImpl(
    private val matchOperationDataSource: MatchOperationDataSource,
) : MatchOperationRepository {
    override suspend fun createOperation(operation: MatchOperation): String = matchOperationDataSource.createOperation(operation)

    override suspend fun updateOperation(operation: MatchOperation) {
        matchOperationDataSource.updateOperation(operation)
    }

    override suspend fun getOperationById(operationId: String): MatchOperation? = matchOperationDataSource.getOperationById(operationId)
}
