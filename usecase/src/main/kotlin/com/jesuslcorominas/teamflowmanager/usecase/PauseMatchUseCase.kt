package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
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

        // Get all player times and pause the ones that are playing
        // Mark them as DESCANSO so we know to resume them later
        val playerTimes = getAllPlayerTimesUseCase().first()
        playerTimes
            .filter { it.status == PlayerTimeStatus.JUGANDO }
            .forEach { playerTime ->
                pausePlayerTimerUseCase(playerTime.playerId, currentTimeMillis)
            }
    }
}
