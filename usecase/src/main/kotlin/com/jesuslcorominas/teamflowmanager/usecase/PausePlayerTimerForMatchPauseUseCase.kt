package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository

interface PausePlayerTimerForMatchPauseUseCase {
    suspend operator fun invoke(
        playerId: Long,
        currentTimeMillis: Long,
    )
}

internal class PausePlayerTimerForMatchPauseUseCaseImpl(
    private val playerTimeRepository: PlayerTimeRepository,
) : PausePlayerTimerForMatchPauseUseCase {
    override suspend fun invoke(
        playerId: Long,
        currentTimeMillis: Long,
    ) {
        playerTimeRepository.pauseTimerForMatchPause(playerId, currentTimeMillis)
    }
}
