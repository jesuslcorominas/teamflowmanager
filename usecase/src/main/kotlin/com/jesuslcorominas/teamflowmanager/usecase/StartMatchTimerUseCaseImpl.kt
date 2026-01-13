package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first


internal class StartMatchTimerUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val matchOperationRepository: MatchOperationRepository,
    private val playerTimeRepository: PlayerTimeRepository,
    private val getMatchByIdUseCase: GetMatchByIdUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
) : StartMatchTimerUseCase {
    override suspend fun invoke(matchId: Long, currentTimeMillis: Long) {
        // Step 1: Create operation with IN_PROGRESS status
        val operation = MatchOperation(
            matchId = matchId,
            type = MatchOperationType.START,
            status = MatchOperationStatus.IN_PROGRESS,
            createdAt = currentTimeMillis
        )
        val operationId = matchOperationRepository.createOperation(operation)

        // Step 2: Update match with operation ID (but not yet as lastCompleted)
        val match = getMatchByIdUseCase(matchId).first()
        if (match != null) {
            val firstNotStartedPeriod = match.periods.first { it.startTimeMillis == 0L }
            val updatedMatch = match.copy(
                status = MatchStatus.IN_PROGRESS,
                periods = match.periods.map { period ->
                    if (period.periodNumber == firstNotStartedPeriod.periodNumber) {
                        period.copy(startTimeMillis = currentTimeMillis)
                    } else {
                        period
                    }
                },
            )
            matchRepository.updateMatch(updatedMatch)

            // Step 3: Update player times with operation ID
            if (match.startingLineupIds.isNotEmpty()) {
                playerTimeRepository.startTimersBatchWithOperationId(
                    match.startingLineupIds,
                    currentTimeMillis,
                    operationId
                )
            }

            // Step 4: Mark operation as COMPLETED
            val completedOperation = operation.copy(
                id = operationId,
                status = MatchOperationStatus.COMPLETED
            )
            matchOperationRepository.updateOperation(completedOperation)

            // Step 5: Update match with lastCompletedOperationId
            matchRepository.updateMatchWithOperationId(updatedMatch, operationId)
        }
    }
}
