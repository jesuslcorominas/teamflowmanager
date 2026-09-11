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

        // Single read of player times for the whole batch. Sets keep the lookups below O(1) while
        // preserving insertion order, which the "other playing players" batch relies on.
        val playerTimes = getAllPlayerTimesUseCase(matchId).first()
        val playingPlayerIds =
            playerTimes
                .filter { it.status == PlayerTimeStatus.PLAYING }
                .map { it.playerId }
                .toSet()
        val knownPlayerIds = playerTimes.map { it.playerId }.toSet()

        val validPairs = selectValidPairs(substitutions, playingPlayerIds, knownPlayerIds)

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

        // Both are already duplicate-free: selectValidPairs consumes each player at most once
        val playerOutIds = validPairs.map { it.playerOutId }
        val playerInIds = validPairs.map { it.playerInId }

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
        // filter (lastOperationId == match.lastCompletedOperationId) keeps showing them. Only the
        // leaving players need excluding: selectValidPairs already guarantees that no incoming
        // player was on the pitch, so none of them can be written twice in the same operation.
        val substitutedOutPlayerIds = playerOutIds.toSet()
        val otherPlayingPlayers = playingPlayerIds.filterNot { it in substitutedOutPlayerIds }
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

    /**
     * Picks the pairs that can be applied together. An invalid pair is silently discarded and never
     * aborts the rest of the batch, which is the historical single-pair behaviour.
     *
     * Every rule is evaluated against the state read at the start of the batch, so the outcome
     * depends on the data and not on the order in which the repository calls happen. A pair is
     * dropped when:
     * - the leaving player is not on the pitch (nothing to substitute out);
     * - the incoming player is unknown to the match, which would make
     *   [PlayerTimeRepository.startTimersBatchWithOperationId] create a brand new PlayerTime row for
     *   a player that was never called up;
     * - the incoming player is already on the pitch. This also covers a self-substitution (A -> A)
     *   and a chained batch (A -> B, B -> C), where benching B and starting B's timer in the same
     *   operation would leave B playing while the history claims B left;
     * - the leaving or the incoming player was already consumed by an earlier pair of the same
     *   batch. Applying only half of such a pair would silently leave the team a player short or a
     *   player long.
     */
    private fun selectValidPairs(
        substitutions: List<SubstitutionPair>,
        playingPlayerIds: Set<String>,
        knownPlayerIds: Set<String>,
    ): List<SubstitutionPair> {
        val validPairs = mutableListOf<SubstitutionPair>()
        val consumedPlayerOutIds = mutableSetOf<String>()
        val consumedPlayerInIds = mutableSetOf<String>()

        substitutions.forEach { pair ->
            val isApplicable =
                pair.playerOutId in playingPlayerIds &&
                    pair.playerInId in knownPlayerIds &&
                    pair.playerInId !in playingPlayerIds &&
                    pair.playerOutId !in consumedPlayerOutIds &&
                    pair.playerInId !in consumedPlayerInIds

            if (isApplicable) {
                validPairs += pair
                consumedPlayerOutIds += pair.playerOutId
                consumedPlayerInIds += pair.playerInId
            }
        }

        return validPairs
    }
}
