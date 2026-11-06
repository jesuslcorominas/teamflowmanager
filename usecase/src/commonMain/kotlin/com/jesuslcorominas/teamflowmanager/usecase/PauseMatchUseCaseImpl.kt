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
import com.jesuslcorominas.teamflowmanager.domain.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first

internal class PauseMatchUseCaseImpl(
    private val matchOperationRepository: MatchOperationRepository,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val playerTimeRepository: PlayerTimeRepository,
    private val getMatchByIdUseCase: GetMatchByIdUseCase,
) : PauseMatchUseCase {
    override suspend fun invoke(
        matchId: String,
        currentTimeMillis: Long,
    ) {
        // Get the match first to validate it exists and is in correct state
        val match = getMatchByIdUseCase(matchId).first()
        if (match == null || match.status != MatchStatus.IN_PROGRESS) {
            return // Match doesn't exist or not in progress, cannot pause
        }

        // Guard: check periods BEFORE creating an operation so no orphaned IN_PROGRESS record
        // is left when this normal path exits early (P2b).
        val firstNotFinishedPeriod = match.periods.firstOrNull { it.endTimeMillis == 0L }
        if (firstNotFinishedPeriod == null) {
            return // All periods already finished, cannot pause
        }

        // Step 1: Create operation with IN_PROGRESS status
        val operation =
            MatchOperation(
                matchId = matchId,
                teamId = match.teamId,
                type = MatchOperationType.PAUSE,
                status = MatchOperationStatus.IN_PROGRESS,
                createdAt = currentTimeMillis,
            )
        val operationId = matchOperationRepository.createOperation(operation)

        // Step 2: Get all player times for this match that are currently playing
        val allPlayerTimes = getAllPlayerTimesUseCase(matchId).first()
        val playingPlayerIds =
            allPlayerTimes
                .filter { it.status == PlayerTimeStatus.PLAYING }
                .map { it.playerId }

        // Step 3: Compute player times without writing
        val playerTimes =
            if (playingPlayerIds.isNotEmpty()) {
                playerTimeRepository.preparePauseTimersBatch(
                    matchId = matchId,
                    playerIds = playingPlayerIds,
                    currentTimeMillis = currentTimeMillis,
                    operationId = operationId,
                )
            } else {
                emptyList()
            }

        // Step 4: Compute updated periods and pauseCount (not written yet — the write happens in
        // the batch below so that if the commit fails, no match state is persisted — P1 fix).
        // pauseCount is incremented here because only the pause operation changes it.
        val updatedPeriods =
            match.periods.map { period ->
                if (period.periodNumber == firstNotFinishedPeriod.periodNumber) {
                    period.copy(endTimeMillis = currentTimeMillis)
                } else {
                    period
                }
            }

        // Step 5: Commit all changes atomically in a single WriteBatch.
        // matchState carries status, periods and pauseCount so they land in the same batch as
        // lastCompletedOperationId — if the commit fails, no match state is persisted (P1 fix).
        val commit =
            MatchOperationCommit(
                matchId = matchId,
                operation = operation.copy(id = operationId, status = MatchOperationStatus.COMPLETED),
                playerTimes = playerTimes,
                lastCompletedOperationId = operationId,
                matchState =
                    MatchStateUpdate(
                        status = MatchStatus.PAUSED,
                        periods = updatedPeriods,
                        pauseCount = match.pauseCount + 1,
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
