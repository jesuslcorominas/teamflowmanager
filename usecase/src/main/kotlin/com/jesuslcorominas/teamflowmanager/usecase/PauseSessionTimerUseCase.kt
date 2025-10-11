package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.SessionRepository

interface PauseSessionTimerUseCase {
    suspend operator fun invoke(currentTimeMillis: Long)
}

internal class PauseSessionTimerUseCaseImpl(
    private val sessionRepository: SessionRepository,
) : PauseSessionTimerUseCase {
    override suspend fun invoke(currentTimeMillis: Long) {
        sessionRepository.pauseTimer(currentTimeMillis)
    }
}
