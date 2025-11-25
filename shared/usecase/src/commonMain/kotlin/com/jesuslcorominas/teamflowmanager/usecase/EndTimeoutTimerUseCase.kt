package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface EndTimeoutTimerUseCase {
    suspend operator fun invoke(matchId: Long, currentTimeMillis: Long)
}

internal class EndTimeoutTimerUseCaseImpl(
    private val matchRepository: MatchRepository,
) : EndTimeoutTimerUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        matchRepository.endTimeout(matchId, currentTimeMillis)
    }
}
