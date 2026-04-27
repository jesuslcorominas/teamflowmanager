package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.domain.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first

internal class FinishMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val matchOperationRepository: MatchOperationRepository,
    private val playerTimeRepository: PlayerTimeRepository,
    private val playerTimeHistoryRepository: PlayerTimeHistoryRepository,
) : FinishMatchUseCase {
    override suspend fun invoke(
        matchId: Long,
        currentTime: Long,
    ) {
        try {
            // Get the current match
            val match = matchRepository.getMatchById(matchId).first()
            if (match == null) {
                return
            }

            if (match.status != MatchStatus.IN_PROGRESS && match.status != MatchStatus.PAUSED) {
                return
            }

            // Step 1: Create operation with IN_PROGRESS status
            val operation =
                MatchOperation(
                    matchId = matchId,
                    teamId = match.teamId,
                    type = MatchOperationType.FINISH,
                    status = MatchOperationStatus.IN_PROGRESS,
                    createdAt = currentTime,
                )
            val operationId = matchOperationRepository.createOperation(operation)

            val periods = match.periods.toMutableList()
            // If the match is in progress, we need to close the current period
            if (match.status == MatchStatus.IN_PROGRESS) {
                val lastPeriodIndex = periods.indexOfLast { it.startTimeMillis > 0L }
                if (lastPeriodIndex != -1) {
                    val lastPeriod = periods[lastPeriodIndex]
                    val updatedLastPeriod =
                        lastPeriod.copy(
                            endTimeMillis = currentTime,
                        )
                    periods[lastPeriodIndex] = updatedLastPeriod
                }
            }

            // Step 2: Update match to mark it as finished (without operation ID yet)
            val finishedMatch =
                match.copy(
                    periods = periods,
                    status = MatchStatus.FINISHED,
                )
            matchRepository.updateMatch(finishedMatch)

            // Step 3: Get player times for this match and save history
            val playerTimes =
                try {
                    playerTimeRepository.getPlayerTimesByMatch(matchId).first()
                } catch (e: Exception) {
                    println("FinishMatchUseCase: Error getting player times: ${e.message}")
                    e.printStackTrace()
                    emptyList()
                }

            // Save player time history
            playerTimes.forEach { playerTime ->
                try {
                    // Calculate final elapsed time if running
                    val finalElapsedTime =
                        if (playerTime.isRunning && playerTime.lastStartTimeMillis != null) {
                            playerTime.elapsedTimeMillis + (currentTime - (playerTime.lastStartTimeMillis ?: 0L))
                        } else {
                            playerTime.elapsedTimeMillis
                        }

                    // Only save if there's time recorded
                    if (finalElapsedTime > 0) {
                        val history =
                            PlayerTimeHistory(
                                playerId = playerTime.playerId,
                                matchId = match.id,
                                elapsedTimeMillis = finalElapsedTime,
                                savedAtMillis = currentTime,
                            )
                        playerTimeHistoryRepository.insertPlayerTimeHistory(history)
                    }
                } catch (e: Exception) {
                    println(
                        "FinishMatchUseCase: Error saving player time history for player ${playerTime.playerId}: ${e.message}",
                    )
                    e.printStackTrace()
                }
            }

            // Step 4: Reset player times in Firestore (best-effort — match result is already saved)
            try {
                playerTimeRepository.resetAllPlayerTimes()
                println("FinishMatchUseCase: Successfully reset all player times")
            } catch (e: Exception) {
                println("FinishMatchUseCase: Error resetting player times (non-fatal): ${e.message}")
                e.printStackTrace()
            }

            // Step 5: Mark operation as COMPLETED
            val completedOperation =
                operation.copy(
                    id = operationId,
                    status = MatchOperationStatus.COMPLETED,
                )
            matchOperationRepository.updateOperation(completedOperation)

            // Step 6: Update match with lastCompletedOperationId
            matchRepository.updateMatchWithOperationId(finishedMatch, operationId)
        } catch (e: Exception) {
            println("FinishMatchUseCase: Unexpected error finishing match: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
