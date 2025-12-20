package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first



internal class ResumeMatchUseCaseImpl(
    private val startMatchTimerUseCase: StartMatchTimerUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val playerTimeRepository: PlayerTimeRepository,
) : ResumeMatchUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        // Get all player times and resume only the ones that were in PAUSED state
        // These are the players who were playing when the match was paused
        val playerTimes = getAllPlayerTimesUseCase().first()
        val pausedPlayerIds = playerTimes
            .filter { it.status == PlayerTimeStatus.PAUSED }
            .map { it.playerId }

        // Start all paused player timers at once using batch operation
        if (pausedPlayerIds.isNotEmpty()) {
            playerTimeRepository.startTimersBatch(pausedPlayerIds, currentTimeMillis)
        }

        // Resume the match timer after player timers
        startMatchTimerUseCase(matchId, currentTimeMillis)
    }
}
