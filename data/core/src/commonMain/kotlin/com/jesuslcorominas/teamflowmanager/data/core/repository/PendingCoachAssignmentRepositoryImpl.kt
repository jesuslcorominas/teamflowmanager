package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PendingCoachAssignmentDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PendingCoachAssignment
import com.jesuslcorominas.teamflowmanager.usecase.repository.PendingCoachAssignmentRepository

internal class PendingCoachAssignmentRepositoryImpl(
    private val dataSource: PendingCoachAssignmentDataSource,
) : PendingCoachAssignmentRepository {
    override suspend fun create(
        teamId: String,
        clubId: String,
        email: String,
    ) {
        dataSource.create(teamId, clubId, email)
    }

    override suspend fun delete(teamId: String) {
        dataSource.delete(teamId)
    }

    override suspend fun getByEmail(email: String): List<PendingCoachAssignment> {
        return dataSource.getByEmail(email)
    }
}
