package com.jesuslcorominas.teamflowmanager.domain.usecase

interface PauseMatchUseCase {
    suspend operator fun invoke(matchId: Long, currentTimeMillis: Long)
}
