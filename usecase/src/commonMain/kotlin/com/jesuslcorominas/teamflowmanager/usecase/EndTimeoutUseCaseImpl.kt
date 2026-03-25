package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first

internal class EndTimeoutUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val playerTimeRepository: PlayerTimeRepository,
) : EndTimeoutUseCase {
    override suspend fun invoke(
        matchId: Long,
        currentTimeMillis: Long,
    ) {
        // Get all player times for this match and resume only the ones that were in PAUSED state
        // These are the players who were playing when the timeout started
        val playerTimes = getAllPlayerTimesUseCase(matchId).first()
        val pausedPlayerIds =
            playerTimes
                .filter { it.status == PlayerTimeStatus.PAUSED }
                .map { it.playerId }

        // Start all paused player timers at once using batch operation
        if (pausedPlayerIds.isNotEmpty()) {
            playerTimeRepository.startTimersBatch(matchId, pausedPlayerIds, currentTimeMillis)
        }

        // End timeout for the match timer after player timers
        matchRepository.endTimeout(matchId, currentTimeMillis)
    }
}
