package com.jesuslcorominas.teamflowmanager.domain.usecase

interface EndTimeoutUseCase {
    suspend operator fun invoke(matchId: Long, currentTimeMillis: Long)
}
