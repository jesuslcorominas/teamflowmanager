package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository

interface PausePlayerTimerUseCase {
    suspend operator fun invoke(
        playerId: Long,
        currentTimeMillis: Long,
    )
}

internal class PausePlayerTimerUseCaseImpl(
    private val playerTimeRepository: PlayerTimeRepository,
) : PausePlayerTimerUseCase {
    override suspend fun invoke(
        playerId: Long,
        currentTimeMillis: Long,
    ) {
        playerTimeRepository.pauseTimer(playerId, currentTimeMillis)
    }
}
