package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import kotlinx.coroutines.flow.first

interface EndTimeoutUseCase {
    suspend operator fun invoke(matchId: Long, currentTimeMillis: Long)
}

internal class EndTimeoutUseCaseImpl(
    private val endTimeoutTimerUseCase: EndTimeoutTimerUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val startPlayerTimerUseCase: StartPlayerTimerUseCase,
) : EndTimeoutUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        // End timeout for the match timer
        endTimeoutTimerUseCase(matchId, currentTimeMillis)

        // Get all player times and resume only the ones that were in PAUSED state
        // These are the players who were playing when the timeout started
        val playerTimes = getAllPlayerTimesUseCase().first()
        playerTimes
            .filter { it.status == PlayerTimeStatus.PAUSED }
            .forEach { playerTime ->
                startPlayerTimerUseCase(playerTime.playerId, currentTimeMillis)
            }
    }
}
