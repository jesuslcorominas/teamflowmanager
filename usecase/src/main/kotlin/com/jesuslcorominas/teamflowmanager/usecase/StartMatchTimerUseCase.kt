package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface StartMatchTimerUseCase {
    suspend operator fun invoke(matchId: Long, currentTimeMillis: Long)
}

internal class StartMatchTimerUseCaseImpl(
    private val matchRepository: MatchRepository,
) : StartMatchTimerUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        matchRepository.startTimer(matchId, currentTimeMillis)
    }
}
