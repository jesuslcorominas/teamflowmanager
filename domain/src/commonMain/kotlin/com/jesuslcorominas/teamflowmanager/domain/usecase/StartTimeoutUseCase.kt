package com.jesuslcorominas.teamflowmanager.domain.usecase

interface StartTimeoutUseCase {
    suspend operator fun invoke(
        matchId: String,
        currentTimeMillis: Long,
    )
}
