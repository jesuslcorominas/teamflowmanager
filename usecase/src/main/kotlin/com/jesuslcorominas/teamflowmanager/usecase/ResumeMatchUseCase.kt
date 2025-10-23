package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import kotlinx.coroutines.flow.first

interface ResumeMatchUseCase {
    suspend operator fun invoke(matchId: Long, currentTimeMillis: Long)
}

internal class ResumeMatchUseCaseImpl(
    private val startMatchTimerUseCase: StartMatchTimerUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val startPlayerTimerUseCase: StartPlayerTimerUseCase,
) : ResumeMatchUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        // Resume the match timer
        startMatchTimerUseCase(matchId, currentTimeMillis)

        // Get all player times and resume only the ones that were in PAUSED state
        // These are the players who were playing when the match was paused
        val playerTimes = getAllPlayerTimesUseCase().first()
        playerTimes
            .filter { it.status == PlayerTimeStatus.PAUSED }
            .forEach { playerTime ->
                startPlayerTimerUseCase(playerTime.playerId, currentTimeMillis)
            }
    }
}
