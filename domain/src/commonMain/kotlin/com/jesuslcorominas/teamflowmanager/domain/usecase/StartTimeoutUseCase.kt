package com.jesuslcorominas.teamflowmanager.domain.usecase

interface StartTimeoutUseCase {
    suspend operator fun invoke(
        matchId: Long,
        currentTimeMillis: Long,
    )
}
