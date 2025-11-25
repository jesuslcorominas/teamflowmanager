package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository

interface StartPlayerTimerUseCase {
    suspend operator fun invoke(
        playerId: Long,
        currentTimeMillis: Long,
    )
}

internal class StartPlayerTimerUseCaseImpl(
    private val playerTimeRepository: PlayerTimeRepository,
) : StartPlayerTimerUseCase {
    override suspend fun invoke(
        playerId: Long,
        currentTimeMillis: Long,
    ) {
        playerTimeRepository.startTimer(playerId, currentTimeMillis)
    }
}
