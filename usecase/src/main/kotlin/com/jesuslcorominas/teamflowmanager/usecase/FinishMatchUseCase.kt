package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first

interface FinishMatchUseCase {
    suspend operator fun invoke(matchId: Long)
}

internal class FinishMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val playerTimeRepository: PlayerTimeRepository,
    private val playerTimeHistoryRepository: PlayerTimeHistoryRepository,
) : FinishMatchUseCase {
    override suspend fun invoke(matchId: Long) {
        val currentTime = System.currentTimeMillis()

        // Get the current match
        val match = matchRepository.getMatchById(matchId).first()
        if (match == null) {
            return
        }

        // Calculate final elapsed time for the match
        val matchFinalElapsedTime = if (match.status == MatchStatus.IN_PROGRESS && match.lastStartTimeMillis != null) {
            match.elapsedTimeMillis + (currentTime - (match.lastStartTimeMillis ?: 0L))
        } else {
            match.elapsedTimeMillis
        }

        // Update match to mark it as finished
        val finishedMatch = match.copy(
            elapsedTimeMillis = matchFinalElapsedTime,
            lastStartTimeMillis = null,
            status = MatchStatus.FINISHED,
        )
        matchRepository.updateMatch(finishedMatch)

        // Get all player times
        val playerTimes = playerTimeRepository.getAllPlayerTimes().first()

        // Save each player time to history
        playerTimes.forEach { playerTime ->
            // Calculate final elapsed time if running
            val finalElapsedTime = if (playerTime.isRunning && playerTime.lastStartTimeMillis != null) {
                playerTime.elapsedTimeMillis + (currentTime - (playerTime.lastStartTimeMillis ?: 0L))
            } else {
                playerTime.elapsedTimeMillis
            }

            // Only save if there's time recorded
            if (finalElapsedTime > 0) {
                val history = PlayerTimeHistory(
                    playerId = playerTime.playerId,
                    matchId = match.id,
                    elapsedTimeMillis = finalElapsedTime,
                    savedAtMillis = currentTime,
                )
                playerTimeHistoryRepository.insertPlayerTimeHistory(history)
            }
        }

        // Reset all player times
        playerTimeRepository.resetAllPlayerTimes()
    }
}
