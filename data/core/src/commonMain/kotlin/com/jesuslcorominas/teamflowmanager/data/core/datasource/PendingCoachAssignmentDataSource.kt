package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.PendingCoachAssignment

interface PendingCoachAssignmentDataSource {
    suspend fun create(teamId: String, clubId: String, email: String)
    suspend fun delete(teamId: String)
    suspend fun getByEmail(email: String): List<PendingCoachAssignment>
}