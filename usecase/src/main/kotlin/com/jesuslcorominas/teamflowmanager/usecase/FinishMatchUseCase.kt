package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first

interface FinishMatchUseCase {
    suspend operator fun invoke(matchId: Long, currentTime: Long)
}

internal class FinishMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val playerTimeRepository: PlayerTimeRepository,
    private val playerTimeHistoryRepository: PlayerTimeHistoryRepository,
    private val transactionRunner: TransactionRunner,
) : FinishMatchUseCase {
    override suspend fun invoke(matchId: Long, currentTime: Long) {
        try {
            // Get the current match
            val match = matchRepository.getMatchById(matchId).first()
            if (match == null) {
                return
            }

            if (match.status != MatchStatus.IN_PROGRESS && match.status != MatchStatus.PAUSED) {
                return
            }

            val periods = match.periods.toMutableList()
            // If the match is in progress, we need to close the current period
            if (match.status == MatchStatus.IN_PROGRESS) {
                val lastPeriodIndex = periods.indexOfLast { it.startTimeMillis > 0L }
                if (lastPeriodIndex != -1) {
                    val lastPeriod = periods[lastPeriodIndex]
                    val updatedLastPeriod = lastPeriod.copy(
                        endTimeMillis = currentTime
                    )
                    periods[lastPeriodIndex] = updatedLastPeriod
                }
            }

            // Update match to mark it as finished
            val finishedMatch = match.copy(
                periods = periods,
                status = MatchStatus.FINISHED,
            )

            val playerTimes = try {
                playerTimeRepository.getAllPlayerTimes().first()
            } catch (e: Exception) {
                // If we can't get player times, log error and continue with empty list
                println("FinishMatchUseCase: Error getting player times: ${e.message}")
                e.printStackTrace()
                emptyList()
            }

            // Save player time history in Room transaction
            transactionRunner.run {
                matchRepository.updateMatch(finishedMatch)

                playerTimes.forEach { playerTime ->
                    try {
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
                    } catch (e: Exception) {
                        // Log error but continue with other players
                        println("FinishMatchUseCase: Error saving player time history for player ${playerTime.playerId}: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }

            // Reset player times in Firestore (outside Room transaction)
            // This must be done separately because Firestore operations don't participate in Room transactions
            try {
                playerTimeRepository.resetAllPlayerTimes()
                println("FinishMatchUseCase: Successfully reset all player times")
            } catch (e: Exception) {
                // Log error but don't fail the finish operation
                println("FinishMatchUseCase: Error resetting player times: ${e.message}")
                e.printStackTrace()
                // Re-throw to make the error more visible
                throw IllegalStateException("Failed to reset player times after finishing match", e)
            }
        } catch (e: Exception) {
            // Catch any unexpected errors to prevent app crash
            println("FinishMatchUseCase: Unexpected error finishing match: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
