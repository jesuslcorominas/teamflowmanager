package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface PauseMatchTimerUseCase {
    suspend operator fun invoke(currentTimeMillis: Long)
}

internal class PauseMatchTimerUseCaseImpl(
    private val matchRepository: MatchRepository,
) : PauseMatchTimerUseCase {
    override suspend fun invoke(currentTimeMillis: Long) {
        matchRepository.pauseTimer(currentTimeMillis)
    }
}
