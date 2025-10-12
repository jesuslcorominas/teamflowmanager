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

        // Get all player times and resume the ones that have time (were active before pause)
        val playerTimes = getAllPlayerTimesUseCase().first()
        playerTimes
            .filter { it.elapsedTimeMillis > 0 }
            .forEach { playerTime ->
                startPlayerTimerUseCase(playerTime.playerId, currentTimeMillis)
            }
    }
}
