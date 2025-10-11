package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface StartMatchTimerUseCase {
    suspend operator fun invoke(currentTimeMillis: Long)
}

internal class StartMatchTimerUseCaseImpl(
    private val matchRepository: MatchRepository,
) : StartMatchTimerUseCase {
    override suspend fun invoke(currentTimeMillis: Long) {
        matchRepository.startTimer(currentTimeMillis)
    }
}
