package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
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
    /**
     * Registers a batch of substitutions under a SINGLE operation id, so that N changes never
     * produce N visible intermediate states.
     */
    override suspend fun invoke(
        matchId: String,
        substitutions: List<SubstitutionPair>,
        currentTimeMillis: Long,
    ) {
        // Nothing to do: exit before touching the repositories
        if (substitutions.isEmpty()) return

        // Get match to calculate elapsed time
        val match = matchRepository.getMatchById(matchId).first()
        requireNotNull(match) { "No active match found" }

        // Single read of player times for the whole batch
        val playerTimes = getAllPlayerTimesUseCase(matchId).first()
        val playingPlayerIds =
            playerTimes
                .filter { it.status == PlayerTimeStatus.PLAYING }
                .map { it.playerId }

        // Keep only pairs whose leaving player is actually on the pitch. An invalid pair is
        // silently discarded and never aborts the batch. distinctBy protects against two pairs
        // sharing the same leaving player: the first one wins.
        val validPairs =
            substitutions
                .filter { it.playerOutId in playingPlayerIds }
                .distinctBy { it.playerOutId }

        // No valid pair: no operation is created
        if (validPairs.isEmpty()) return

        // Elapsed time computed once and shared by every PlayerSubstitution of the batch
        val matchElapsedTime = match.getTotalElapsed(currentTimeMillis)

        // Step 1: Create a SINGLE operation with IN_PROGRESS status for the whole batch
        val operation =
            MatchOperation(
                matchId = matchId,
                teamId = match.teamId,
                type = MatchOperationType.SUBSTITUTION,
                status = MatchOperationStatus.IN_PROGRESS,
            )
        val operationId = matchOperationRepository.createOperation(operation)

        val playerOutIds = validPairs.map { it.playerOutId }
        val playerInIds = validPairs.map { it.playerInId }.distinct()

        // Step 2: Substitute out every leaving player in one call - sets ON_BENCH status
        playerTimeRepository.substituteOutPlayersBatchWithOperationId(
            matchId = matchId,
            playerIds = playerOutIds,
            currentTimeMillis = currentTimeMillis,
            operationId = operationId,
        )

        // Step 3: Start timers for every incoming player in one call
        playerTimeRepository.startTimersBatchWithOperationId(
            matchId = matchId,
            playerIds = playerInIds,
            currentTimeMillis = currentTimeMillis,
            operationId = operationId,
        )

        // Step 4: Refresh the operationId of the remaining players still on the pitch, so the UI
        // filter (lastOperationId == match.lastCompletedOperationId) keeps showing them. Incoming
        // players are excluded too, to avoid writing twice on the same PlayerTime within the same
        // operation if an incoming player was already PLAYING.
        val otherPlayingPlayers =
            playingPlayerIds.filterNot { it in playerOutIds || it in playerInIds }
        if (otherPlayingPlayers.isNotEmpty()) {
            playerTimeRepository.startTimersBatchWithOperationId(
                matchId = matchId,
                playerIds = otherPlayingPlayers,
                currentTimeMillis = currentTimeMillis,
                operationId = operationId,
            )
        }

        // Step 5: Record one substitution per valid pair, all sharing operationId and elapsed time
        validPairs.forEach { pair ->
            playerSubstitutionRepository.insertSubstitution(
                PlayerSubstitution(
                    matchId = matchId,
                    playerOutId = pair.playerOutId,
                    playerInId = pair.playerInId,
                    substitutionTimeMillis = currentTimeMillis,
                    matchElapsedTimeMillis = matchElapsedTime,
                    operationId = operationId,
                ),
            )
        }

        // Step 6: Mark operation as COMPLETED
        matchOperationRepository.updateOperation(
            operation.copy(
                id = operationId,
                status = MatchOperationStatus.COMPLETED,
            ),
        )

        // Step 7: Update match's lastCompletedOperationId
        matchRepository.updateMatchWithOperationId(
            match = match.copy(lastCompletedOperationId = operationId),
            operationId = operationId,
        )
    }
}
