package com.jesuslcorominas.teamflowmanager.usecase

import kotlinx.coroutines.flow.first

interface ResumeMatchUseCase {
    suspend operator fun invoke(currentTimeMillis: Long)
}

internal class ResumeMatchUseCaseImpl(
    private val startMatchTimerUseCase: StartMatchTimerUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val startPlayerTimerUseCase: StartPlayerTimerUseCase,
) : ResumeMatchUseCase {
    override suspend fun invoke(currentTimeMillis: Long) {
        // Resume the match timer
        startMatchTimerUseCase(currentTimeMillis)

        // Get all player times and resume the ones that were running when match was paused
        // Players who were running will have lastStartTimeMillis != null after pause
        val playerTimes = getAllPlayerTimesUseCase().first()
        playerTimes
            .filter { it.lastStartTimeMillis != null }
            .forEach { playerTime ->
                startPlayerTimerUseCase(playerTime.playerId, currentTimeMillis)
            }
    }
}
