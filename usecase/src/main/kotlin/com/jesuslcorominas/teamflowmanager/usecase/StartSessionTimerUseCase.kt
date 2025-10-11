package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.SessionRepository

interface StartSessionTimerUseCase {
    suspend operator fun invoke(currentTimeMillis: Long)
}

internal class StartSessionTimerUseCaseImpl(
    private val sessionRepository: SessionRepository,
) : StartSessionTimerUseCase {
    override suspend fun invoke(currentTimeMillis: Long) {
        sessionRepository.startTimer(currentTimeMillis)
    }
}
