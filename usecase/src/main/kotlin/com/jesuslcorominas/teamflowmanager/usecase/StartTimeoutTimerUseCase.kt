package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface StartTimeoutTimerUseCase {
    suspend operator fun invoke(matchId: Long, currentTimeMillis: Long)
}

internal class StartTimeoutTimerUseCaseImpl(
    private val matchRepository: MatchRepository,
) : StartTimeoutTimerUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        matchRepository.startTimeout(matchId, currentTimeMillis)
    }
}
