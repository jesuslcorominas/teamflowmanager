package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.PendingCoachAssignment

interface PendingCoachAssignmentRepository {
    suspend fun create(
        teamId: String,
        clubId: String,
        email: String,
    )

    suspend fun delete(teamId: String)

    suspend fun getByEmail(email: String): List<PendingCoachAssignment>
}
