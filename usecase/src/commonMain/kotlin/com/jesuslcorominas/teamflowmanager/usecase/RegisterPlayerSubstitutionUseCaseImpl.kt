package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.DiscardedSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationCommit
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionBatchResult
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionDiscardReason
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first

internal class RegisterPlayerSubstitutionUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val playerTimeRepository: PlayerTimeRepository,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val matchOperationRepository: MatchOperationRepository,
) : RegisterPlayerSubstitutionUseCase {
    /**
     * Registers a batch of substitutions under a SINGLE atomic operation, so that N changes
     * never produce N visible intermediate states (#423).
     *
     * All PlayerTime records (substituted out, substituted in, remaining on pitch) and the
     * lastCompletedOperationId are written in one WriteBatch via [MatchOperationRepository.commitOperation].
     * If the commit fails the operation is marked FAILED and the exception is re-propagated (#416).
     */
    override suspend fun invoke(
        matchId: String,
        substitutions: List<SubstitutionPair>,
        currentTimeMillis: Long,
    ): SubstitutionBatchResult {
        // Nothing to do: exit before touching the repositories
        if (substitutions.isEmpty()) {
            return SubstitutionBatchResult(applied = emptyList(), discarded = emptyList())
        }

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

        // The squad call-up is the source of truth for who may come on, NOT the player times:
        // those rows are created lazily the first time a player's timer starts, so a called-up
        // substitute who has not played yet has no row at all.
        val selection = selectPairs(substitutions, playingPlayerIds, match.squadCallUpIds.toSet())
        val validPairs = selection.applied

        // No valid pair: no operation is created, but the caller still learns why
        if (validPairs.isEmpty()) return selection

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

        // Both are already duplicate-free: selectPairs consumes each player at most once
        val playerOutIds = validPairs.map { it.playerOutId }
        val playerInIds = validPairs.map { it.playerInId }

        // Step 2: Compute outgoing player times without writing
        val playerOutTimes =
            playerTimeRepository.prepareSubstituteOutPlayersBatch(
                matchId = matchId,
                playerIds = playerOutIds,
                currentTimeMillis = currentTimeMillis,
                operationId = operationId,
            )

        // Step 3: Compute incoming player times without writing
        val playerInTimes =
            playerTimeRepository.prepareStartTimersBatch(
                matchId = matchId,
                playerIds = playerInIds,
                currentTimeMillis = currentTimeMillis,
                operationId = operationId,
            )

        // Step 4: Refresh the operationId of the remaining players still on the pitch, so the UI
        // filter (lastOperationId == match.lastCompletedOperationId) keeps showing them. Only the
        // leaving players need excluding: selectPairs already guarantees that no incoming
        // player was on the pitch, so none of them can be written twice in the same operation.
        val substitutedOutPlayerIds = playerOutIds.toSet()
        val otherPlayingPlayers = playingPlayerIds.filterNot { it in substitutedOutPlayerIds }
        val otherTimes =
            if (otherPlayingPlayers.isNotEmpty()) {
                playerTimeRepository.prepareStartTimersBatch(
                    matchId = matchId,
                    playerIds = otherPlayingPlayers,
                    currentTimeMillis = currentTimeMillis,
                    operationId = operationId,
                )
            } else {
                emptyList()
            }

        // Step 5: Build substitution records (all sharing operationId and elapsed time)
        val playerSubstitutions =
            validPairs.map { pair ->
                PlayerSubstitution(
                    matchId = matchId,
                    playerOutId = pair.playerOutId,
                    playerInId = pair.playerInId,
                    substitutionTimeMillis = currentTimeMillis,
                    matchElapsedTimeMillis = matchElapsedTime,
                    operationId = operationId,
                )
            }

        // Step 6: Commit all changes atomically in a single WriteBatch
        val commit =
            MatchOperationCommit(
                matchId = matchId,
                operation = operation.copy(id = operationId, status = MatchOperationStatus.COMPLETED),
                playerTimes = playerOutTimes + playerInTimes + otherTimes,
                substitutions = playerSubstitutions,
                lastCompletedOperationId = operationId,
            )

        try {
            matchOperationRepository.commitOperation(commit)
        } catch (e: Exception) {
            matchOperationRepository.updateOperation(
                operation.copy(id = operationId, status = MatchOperationStatus.FAILED),
            )
            throw e
        }

        return selection
    }

    /**
     * Splits the requested batch into the pairs that can be applied together and the ones that
     * cannot, each with its [SubstitutionDiscardReason]. An invalid pair is discarded and never
     * aborts the rest of the batch, which is the historical single-pair behaviour.
     *
     * Every rule is evaluated against the state read at the start of the batch, so the outcome
     * depends on the data and not on the order in which the repository calls happen. Both lists
     * preserve the order of [substitutions].
     */
    private fun selectPairs(
        substitutions: List<SubstitutionPair>,
        playingPlayerIds: Set<String>,
        calledUpPlayerIds: Set<String>,
    ): SubstitutionBatchResult {
        val applied = mutableListOf<SubstitutionPair>()
        val discarded = mutableListOf<DiscardedSubstitution>()
        val consumedPlayerOutIds = mutableSetOf<String>()
        val consumedPlayerInIds = mutableSetOf<String>()

        substitutions.forEach { pair ->
            // Both ids are checked before either is consumed, so rejecting a pair because of its
            // incoming player does not burn a leaving player that a later pair could still use
            val reason =
                discardReasonFor(
                    pair = pair,
                    playingPlayerIds = playingPlayerIds,
                    calledUpPlayerIds = calledUpPlayerIds,
                    consumedPlayerOutIds = consumedPlayerOutIds,
                    consumedPlayerInIds = consumedPlayerInIds,
                )

            if (reason == null) {
                applied += pair
                consumedPlayerOutIds += pair.playerOutId
                consumedPlayerInIds += pair.playerInId
            } else {
                discarded += DiscardedSubstitution(pair = pair, reason = reason)
            }
        }

        return SubstitutionBatchResult(applied = applied, discarded = discarded)
    }

    /**
     * The reason why [pair] cannot be applied, or `null` when it can. When a pair breaks more than
     * one rule the first match wins, so the branches below follow the declaration order of
     * [SubstitutionDiscardReason].
     */
    private fun discardReasonFor(
        pair: SubstitutionPair,
        playingPlayerIds: Set<String>,
        calledUpPlayerIds: Set<String>,
        consumedPlayerOutIds: Set<String>,
        consumedPlayerInIds: Set<String>,
    ): SubstitutionDiscardReason? =
        when {
            pair.playerOutId !in playingPlayerIds ->
                SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING

            pair.playerInId in playingPlayerIds ->
                SubstitutionDiscardReason.PLAYER_IN_ALREADY_PLAYING

            pair.playerInId !in calledUpPlayerIds ->
                SubstitutionDiscardReason.PLAYER_IN_NOT_IN_MATCH

            pair.playerOutId in consumedPlayerOutIds || pair.playerInId in consumedPlayerInIds ->
                SubstitutionDiscardReason.PLAYER_ALREADY_SUBSTITUTED_IN_BATCH

            else -> null
        }
}
