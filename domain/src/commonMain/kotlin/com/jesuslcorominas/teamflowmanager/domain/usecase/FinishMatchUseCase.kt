package com.jesuslcorominas.teamflowmanager.domain.usecase

interface FinishMatchUseCase {
    suspend operator fun invoke(
        matchId: String,
        currentTime: Long,
    )
}
