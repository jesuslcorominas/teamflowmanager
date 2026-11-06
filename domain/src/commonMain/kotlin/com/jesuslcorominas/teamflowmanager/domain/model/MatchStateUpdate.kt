package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * Carries the match state fields that must be written atomically together with the operation
 * commit. When present in [MatchOperationCommit.matchState], the Firestore batch will include
 * [status], [periods] and optionally [pauseCount] in the same partial update that writes
 * [Match.lastCompletedOperationId], so that either all fields land or none do.
 *
 * [pauseCount] is optional — only the pause operation changes it. Pass `null` to leave the
 * field untouched in the backing store.
 */
data class MatchStateUpdate(
    val status: MatchStatus,
    val periods: List<MatchPeriod>,
    val pauseCount: Int? = null,
)
