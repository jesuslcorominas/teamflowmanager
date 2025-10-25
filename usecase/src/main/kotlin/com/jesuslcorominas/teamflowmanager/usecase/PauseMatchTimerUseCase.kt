package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface PauseMatchTimerUseCase {
    suspend operator fun invoke(matchId: Long, currentTimeMillis: Long)
}

internal class PauseMatchTimerUseCaseImpl(
    private val matchRepository: MatchRepository,
) : PauseMatchTimerUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        matchRepository.pauseTimer(matchId, currentTimeMillis)
    }
}
