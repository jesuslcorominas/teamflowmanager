package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.PausePlayerTimerForMatchPauseUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.first



internal class PauseMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val pausePlayerTimerForMatchPauseUseCase: PausePlayerTimerForMatchPauseUseCase,
    private val transactionRunner: TransactionRunner
) : PauseMatchUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        transactionRunner.run {
            // Pause the match timer
            matchRepository.pauseTimer(matchId, currentTimeMillis)

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
}
