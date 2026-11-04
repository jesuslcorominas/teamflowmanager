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
)
