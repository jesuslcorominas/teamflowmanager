package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import kotlinx.coroutines.flow.first

interface StartTimeoutUseCase {
    suspend operator fun invoke(matchId: Long, currentTimeMillis: Long)
}

internal class StartTimeoutUseCaseImpl(
    private val startTimeoutTimerUseCase: StartTimeoutTimerUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val pausePlayerTimerForMatchPauseUseCase: PausePlayerTimerForMatchPauseUseCase,
) : StartTimeoutUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        // Start timeout for the match timer
        startTimeoutTimerUseCase(matchId, currentTimeMillis)

        // Get all player times and pause the ones that are playing
        // Mark them as PAUSED so we know to resume them later
        val playerTimes = getAllPlayerTimesUseCase().first()
        playerTimes
            .filter { it.status == PlayerTimeStatus.PLAYING }
            .forEach { playerTime ->
                pausePlayerTimerForMatchPauseUseCase(playerTime.playerId, currentTimeMillis)
            }
    }
}
