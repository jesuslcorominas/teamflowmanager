package com.jesuslcorominas.teamflowmanager.domain.usecase

interface CreatePendingCoachAssignmentUseCase {
    suspend operator fun invoke(
        teamId: String,
        email: String,
    )
}
