package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationCommit
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStateUpdate
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first

internal class ResumeMatchUseCaseImpl(
    private val matchOperationRepository: MatchOperationRepository,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val playerTimeRepository: PlayerTimeRepository,
    private val getMatchByIdUseCase: GetMatchByIdUseCase,
) : ResumeMatchUseCase {
    override suspend fun invoke(
        matchId: String,
        currentTimeMillis: Long,
    ) {
        // Get the match first to validate it exists
        val match = getMatchByIdUseCase(matchId).first()
        if (match == null) {
            return // Match doesn't exist, cannot resume
        }

        // Guard: check periods BEFORE creating an operation so no orphaned IN_PROGRESS record
        // is left when this normal path exits early (P2b).
        val firstNotStartedPeriod = match.periods.firstOrNull { it.startTimeMillis == 0L }
        if (firstNotStartedPeriod == null) {
            return // All periods already started, cannot resume to start a new period
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

        // Step 2: Get all player times for this match and resume only the ones in PAUSED state
        val allPlayerTimes = getAllPlayerTimesUseCase(matchId).first()
        val pausedPlayerIds =
            allPlayerTimes
                .filter { it.status == PlayerTimeStatus.PAUSED }
                .map { it.playerId }

        // Step 3: Compute player times without writing
        val playerTimes =
            if (pausedPlayerIds.isNotEmpty()) {
                playerTimeRepository.prepareStartTimersBatch(
                    matchId = matchId,
                    playerIds = pausedPlayerIds,
                    currentTimeMillis = currentTimeMillis,
                    operationId = operationId,
                )
            } else {
                emptyList()
            }

        // Step 4: Compute updated periods (not written yet — the write happens in the batch below).
        val updatedPeriods =
            match.periods.map { period ->
                if (period.periodNumber == firstNotStartedPeriod.periodNumber) {
                    period.copy(startTimeMillis = currentTimeMillis)
                } else {
                    period
                }
            }

        // Step 5: Commit all changes atomically in a single WriteBatch.
        // matchState carries status and periods so they land in the same batch as
        // lastCompletedOperationId — if the commit fails, no match state is persisted (P1 fix).
        val commit =
            MatchOperationCommit(
                matchId = matchId,
                operation = operation.copy(id = operationId, status = MatchOperationStatus.COMPLETED),
                playerTimes = playerTimes,
                lastCompletedOperationId = operationId,
                matchState =
                    MatchStateUpdate(
                        status = MatchStatus.IN_PROGRESS,
                        periods = updatedPeriods,
                    ),
            )

        try {
            matchOperationRepository.commitOperation(commit)
        } catch (e: Exception) {
            // Mark the operation as FAILED so the sweep can clean it up. If this secondary write
            // also fails, suppress it and re-throw the original exception so the caller always
            // receives the root cause (P2a).
            try {
                matchOperationRepository.updateOperation(
                    operation.copy(id = operationId, status = MatchOperationStatus.FAILED),
                )
            } catch (updateEx: Exception) {
                println("MatchOperation: could not mark operation $operationId as FAILED: ${updateEx.message}")
            }
            throw e
        }
    }
}
