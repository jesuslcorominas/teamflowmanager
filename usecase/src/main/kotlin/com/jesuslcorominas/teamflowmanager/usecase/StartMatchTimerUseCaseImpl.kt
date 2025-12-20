package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository


internal class StartMatchTimerUseCaseImpl(
    private val matchRepository: MatchRepository,
) : StartMatchTimerUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        matchRepository.startTimer(matchId, currentTimeMillis)
    }
}
