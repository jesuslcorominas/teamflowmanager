package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first

internal class ResumeMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val matchOperationRepository: MatchOperationRepository,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val playerTimeRepository: PlayerTimeRepository,
    private val getMatchByIdUseCase: GetMatchByIdUseCase,
) : ResumeMatchUseCase {
    override suspend fun invoke(
        matchId: Long,
        currentTimeMillis: Long,
    ) {
        // Get the match first to validate it exists
        val match = getMatchByIdUseCase(matchId).first()
        if (match == null) {
            return // Match doesn't exist, cannot resume
        }

        // Step 1: Create operation with IN_PROGRESS status
        val operation =
            MatchOperation(
                matchId = matchId,
                teamId = match.teamId,
                type = MatchOperationType.RESUME,
                status = MatchOperationStatus.IN_PROGRESS,
                createdAt = currentTimeMillis,
            )
        val operationId = matchOperationRepository.createOperation(operation)

        // Step 2: Get all player times for this match and resume only the ones that were in PAUSED state
        val playerTimes = getAllPlayerTimesUseCase(matchId).first()
        val pausedPlayerIds =
            playerTimes
                .filter { it.status == PlayerTimeStatus.PAUSED }
                .map { it.playerId }

        // Step 3: Start all paused player timers with operation ID
        if (pausedPlayerIds.isNotEmpty()) {
            playerTimeRepository.startTimersBatchWithOperationId(
                matchId = matchId,
                playerIds = pausedPlayerIds,
                currentTimeMillis = currentTimeMillis,
                operationId = operationId,
            )
        }

        // Step 4: Resume the match timer
        // For resume, find the next period that hasn't started yet
        val firstNotStartedPeriod = match.periods.firstOrNull { it.startTimeMillis == 0L }
        if (firstNotStartedPeriod == null) {
            return // All periods already started, cannot resume to start a new period
        }

        val updatedMatch =
            match.copy(
                status = MatchStatus.IN_PROGRESS,
                periods =
                    match.periods.map { period ->
                        if (period.periodNumber == firstNotStartedPeriod.periodNumber) {
                            period.copy(startTimeMillis = currentTimeMillis)
                        } else {
                            period
                        }
                    },
            )
        matchRepository.updateMatch(updatedMatch)

        // Step 5: Mark operation as COMPLETED
        val completedOperation =
            operation.copy(
                id = operationId,
                status = MatchOperationStatus.COMPLETED,
            )
        matchOperationRepository.updateOperation(completedOperation)

        // Step 6: Update match with lastCompletedOperationId
        matchRepository.updateMatchWithOperationId(updatedMatch, operationId)
    }
}
