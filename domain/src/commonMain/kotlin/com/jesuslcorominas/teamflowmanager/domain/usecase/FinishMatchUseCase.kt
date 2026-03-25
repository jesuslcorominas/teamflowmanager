package com.jesuslcorominas.teamflowmanager.domain.usecase

interface FinishMatchUseCase {
    suspend operator fun invoke(
        matchId: Long,
        currentTime: Long,
    )
}
