package com.jesuslcorominas.teamflowmanager.usecase

import kotlinx.coroutines.flow.first

interface PauseMatchUseCase {
    suspend operator fun invoke(currentTimeMillis: Long)
}

internal class PauseMatchUseCaseImpl(
    private val pauseMatchTimerUseCase: PauseMatchTimerUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val pausePlayerTimerUseCase: PausePlayerTimerUseCase,
) : PauseMatchUseCase {
    override suspend fun invoke(currentTimeMillis: Long) {
        // Pause the match timer
        pauseMatchTimerUseCase(currentTimeMillis)

        // Get all player times and pause the ones that are running
        val playerTimes = getAllPlayerTimesUseCase().first()
        playerTimes
            .filter { it.isRunning }
            .forEach { playerTime ->
                pausePlayerTimerUseCase(playerTime.playerId, currentTimeMillis)
            }
    }
}
