package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * Represents a fully prepared match operation ready to be persisted as a single atomic write.
 *
 * All fields are computed before any persistence occurs, so either the entire commit lands
 * or nothing is written. This prevents the partial-state windows that caused #423 (empty
 * field between writes) and makes #416-style orphaned IN_PROGRESS states structurally
 * impossible during normal execution.
 */
data class MatchOperationCommit(
    val matchId: String,
    /** The operation to persist, already set to [MatchOperationStatus.COMPLETED]. */
    val operation: MatchOperation,
    /** All [PlayerTime] records affected by this operation, with updated elapsed times and operation id. */
    val playerTimes: List<PlayerTime>,
    /** Substitutions created by this operation, if any. Defaults to empty for non-substitution operations. */
    val substitutions: List<PlayerSubstitution> = emptyList(),
    /**
     * The operation id that must be written to [Match.lastCompletedOperationId] as a partial
     * field update (never a full document replace, to avoid overwriting concurrent writes — #417).
     */
    val lastCompletedOperationId: String,
    /**
     * When non-null, the batch will also write [MatchStateUpdate.status], [MatchStateUpdate.periods]
     * and optionally [MatchStateUpdate.pauseCount] into the Match document in the same atomic write
     * as [lastCompletedOperationId]. This closes the P1 partial-state window: if the batch fails,
     * none of the match state fields are persisted.
     *
     * Callers that do not need to advance match state (e.g. substitution, goal) leave this null.
     */
    val matchState: MatchStateUpdate? = null,
)
