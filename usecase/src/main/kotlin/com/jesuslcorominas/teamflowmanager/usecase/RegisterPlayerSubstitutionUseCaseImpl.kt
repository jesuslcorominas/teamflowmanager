package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first



internal class RegisterPlayerSubstitutionUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val playerTimeRepository: PlayerTimeRepository,
    private val playerSubstitutionRepository: PlayerSubstitutionRepository,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val matchOperationRepository: MatchOperationRepository,
) : RegisterPlayerSubstitutionUseCase {
    override suspend fun invoke(
        matchId: Long,
        playerOutId: Long,
        playerInId: Long,
        currentTimeMillis: Long,
    ) {
        // Get match to calculate elapsed time
        val match = matchRepository.getMatchById(matchId).first()
        requireNotNull(match) { "No active match found" }

        // Get all player times to check if playerOut is actually playing
        val playerTimes = getAllPlayerTimesUseCase().first()
        val playerOutTime = playerTimes.find { it.playerId == playerOutId }

        // Only proceed if the player being substituted out is currently playing (status PLAYING)
        if (playerOutTime?.status != PlayerTimeStatus.PLAYING) {
            return
        }

        val matchElapsedTime = match.getTotalElapsed(currentTimeMillis)

        // Step 1: Create operation with IN_PROGRESS status
        val operation = MatchOperation(
            matchId = matchId,
            teamId = match.teamId,
            type = MatchOperationType.SUBSTITUTION,
            status = MatchOperationStatus.IN_PROGRESS,
        )
        val operationId = matchOperationRepository.createOperation(operation)

        // Step 2: Update ALL currently playing players' operationId for atomicity
        // This ensures that the UI filter (lastOperationId == match.lastCompletedOperationId)
        // will show all active players consistently after the substitution completes
        val playingPlayerIds = playerTimes
            .filter { it.status == PlayerTimeStatus.PLAYING }
            .map { it.playerId }
        
        // Substitute out the leaving player - sets ON_BENCH status
        playerTimeRepository.substituteOutPlayersBatchWithOperationId(
            playerIds = listOf(playerOutId),
            currentTimeMillis = currentTimeMillis,
            operationId = operationId,
        )

        // Start timer for player coming in with operation ID
        playerTimeRepository.startTimersBatchWithOperationId(
            playerIds = listOf(playerInId),
            currentTimeMillis = currentTimeMillis,
            operationId = operationId,
        )
        
        // Update all OTHER currently playing players (excluding playerOut who was just benched)
        // with the new operationId to maintain consistency
        val otherPlayingPlayers = playingPlayerIds.filter { it != playerOutId }
        if (otherPlayingPlayers.isNotEmpty()) {
            playerTimeRepository.startTimersBatchWithOperationId(
                playerIds = otherPlayingPlayers,
                currentTimeMillis = currentTimeMillis,
                operationId = operationId,
            )
        }

        // Step 4: Record the substitution with operation ID
        val substitution = PlayerSubstitution(
            matchId = matchId,
            playerOutId = playerOutId,
            playerInId = playerInId,
            substitutionTimeMillis = currentTimeMillis,
            matchElapsedTimeMillis = matchElapsedTime,
            operationId = operationId,
        )

        playerSubstitutionRepository.insertSubstitution(substitution)

        // Step 5: Mark operation as COMPLETED
        matchOperationRepository.updateOperation(
            operation.copy(
                id = operationId,
                status = MatchOperationStatus.COMPLETED,
            ),
        )

        // Step 6: Update match's lastCompletedOperationId
        matchRepository.updateMatchWithOperationId(
            match = match.copy(lastCompletedOperationId = operationId),
            operationId = operationId,
        )
    }
}
