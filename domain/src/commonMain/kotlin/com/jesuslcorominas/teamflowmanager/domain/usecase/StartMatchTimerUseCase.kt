package com.jesuslcorominas.teamflowmanager.domain.usecase

interface StartMatchTimerUseCase {
    suspend operator fun invoke(
        matchId: Long,
        currentTimeMillis: Long,
    )
}
