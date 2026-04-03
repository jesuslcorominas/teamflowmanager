package com.jesuslcorominas.teamflowmanager.domain.usecase

interface DeletePendingCoachAssignmentUseCase {
    suspend operator fun invoke(teamId: String)
}
