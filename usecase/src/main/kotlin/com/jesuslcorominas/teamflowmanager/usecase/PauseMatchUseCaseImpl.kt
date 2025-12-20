package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first



internal class PauseMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val playerTimeRepository: PlayerTimeRepository,
) : PauseMatchUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        // Get all player times that are currently playing
        val playerTimes = getAllPlayerTimesUseCase().first()
        val playingPlayerIds = playerTimes
            .filter { it.status == PlayerTimeStatus.PLAYING }
            .map { it.playerId }

        // Pause all playing player timers at once using batch operation
        if (playingPlayerIds.isNotEmpty()) {
            playerTimeRepository.pauseTimersBatch(playingPlayerIds, currentTimeMillis)
        }

        // Pause the match timer after player timers
        matchRepository.pauseTimer(matchId, currentTimeMillis)
    }
}
