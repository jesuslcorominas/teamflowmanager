package com.jesuslcorominas.teamflowmanager.domain.usecase

interface ResolvePendingCoachAssignmentsForUserUseCase {
    suspend operator fun invoke(
        userId: String,
        userEmail: String,
    )
}
