package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationCommit

interface MatchOperationRepository {
    /**
     * Creates a new operation and returns its ID.
     */
    suspend fun createOperation(operation: MatchOperation): String

    /**
     * Updates an existing operation.
     */
    suspend fun updateOperation(operation: MatchOperation)

    /**
     * Gets an operation by its ID.
     */
    suspend fun getOperationById(operationId: String): MatchOperation?

    /**
     * Atomically persists all data in [commit] in a single write batch.
     *
     * The batch includes: all [MatchOperationCommit.playerTimes], any
     * [MatchOperationCommit.substitutions], the [MatchOperationCommit.operation] marked as
     * COMPLETED, and a **partial** field update of `Match.lastCompletedOperationId` — never a
     * full document replace, to avoid overwriting concurrent writes on other fields (#417).
     *
     * Either every write in the batch lands or none do, eliminating the partial-state window
     * described in #423.
     */
    suspend fun commitOperation(commit: MatchOperationCommit)

    /**
     * Marks as FAILED all IN_PROGRESS operations on [matchId] whose [MatchOperation.createdAt]
     * is older than [olderThanMillis] milliseconds ago (i.e. `createdAt < now - olderThanMillis`).
     *
     * The age threshold is mandatory: without it, an operation that is legitimately in progress on
     * another device of the same team could be incorrectly marked as FAILED while still alive. By
     * only sweeping operations older than a configured threshold, callers can distinguish a truly
     * orphaned operation (from a crashed session) from one that is simply running on another device.
     * The concrete threshold value is determined by the caller (see T005).
     *
     * This method does **not** modify `Match.lastCompletedOperationId`; it only transitions
     * operation status to FAILED so the UI stops blocking on a stale IN_PROGRESS marker.
     *
     * @param matchId   The match whose stale operations should be swept.
     * @param olderThanMillis   Operations created more than this many milliseconds ago are eligible
     *                          for the sweep. Must be a positive value.
     */
    suspend fun markStaleOperationsAsFailed(
        matchId: String,
        olderThanMillis: Long,
    )
}
