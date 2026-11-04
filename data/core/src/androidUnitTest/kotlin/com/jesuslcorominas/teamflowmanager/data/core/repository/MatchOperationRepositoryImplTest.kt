package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchOperationDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationCommit
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class MatchOperationRepositoryImplTest {

    private lateinit var matchOperationDataSource: MatchOperationDataSource

    // Fixed "now" injected into the repository so threshold tests are deterministic.
    private var fakeNow: Long = 0L
    private lateinit var repository: MatchOperationRepositoryImpl

    @Before
    fun setup() {
        matchOperationDataSource = mockk(relaxed = true)
        fakeNow = 10 * 60 * 1000L // arbitrary baseline: 10 minutes in ms
        repository = MatchOperationRepositoryImpl(matchOperationDataSource) { fakeNow }
    }

    private fun createOperation(
        id: String = "",
        matchId: String = "1",
        teamId: String = "1",
        type: MatchOperationType = MatchOperationType.START,
        status: MatchOperationStatus = MatchOperationStatus.IN_PROGRESS,
        createdAt: Long = fakeNow,
    ) = MatchOperation(
        id = id,
        matchId = matchId,
        teamId = teamId,
        type = type,
        status = status,
        createdAt = createdAt,
    )

    // --- createOperation ---

    @Test
    fun `givenOperation_whenCreateOperation_thenReturnsGeneratedId`() = runTest {
        val operation = createOperation(type = MatchOperationType.START)
        val expectedId = "op-firestore-id-123"
        coEvery { matchOperationDataSource.createOperation(operation) } returns expectedId

        val result = repository.createOperation(operation)

        assertEquals(expectedId, result)
        coVerify { matchOperationDataSource.createOperation(operation) }
    }

    @Test
    fun `givenSubstitutionOperation_whenCreateOperation_thenDelegatesToDataSource`() = runTest {
        val operation = createOperation(type = MatchOperationType.SUBSTITUTION)
        coEvery { matchOperationDataSource.createOperation(operation) } returns "op-sub-456"

        val result = repository.createOperation(operation)

        assertEquals("op-sub-456", result)
    }

    // --- updateOperation ---

    @Test
    fun `givenCompletedOperation_whenUpdateOperation_thenDelegatesToDataSource`() = runTest {
        val operation = createOperation(id = "op-123", status = MatchOperationStatus.COMPLETED)

        repository.updateOperation(operation)

        coVerify { matchOperationDataSource.updateOperation(operation) }
    }

    @Test
    fun `givenPauseOperation_whenUpdateOperation_thenDelegatesToDataSource`() = runTest {
        val operation = createOperation(type = MatchOperationType.PAUSE, status = MatchOperationStatus.IN_PROGRESS)

        repository.updateOperation(operation)

        coVerify { matchOperationDataSource.updateOperation(operation) }
    }

    // --- getOperationById ---

    @Test
    fun `givenExistingOperationId_whenGetOperationById_thenReturnsOperation`() = runTest {
        val operationId = "op-existing-123"
        val operation = createOperation(id = operationId)
        coEvery { matchOperationDataSource.getOperationById(operationId) } returns operation

        val result = repository.getOperationById(operationId)

        assertEquals(operation, result)
        coVerify { matchOperationDataSource.getOperationById(operationId) }
    }

    @Test
    fun `givenUnknownOperationId_whenGetOperationById_thenReturnsNull`() = runTest {
        coEvery { matchOperationDataSource.getOperationById("unknown-id") } returns null

        val result = repository.getOperationById("unknown-id")

        assertNull(result)
    }

    // -----------------------------------------------------------------------------------------
    // Regression tests — #416: commitOperation
    // -----------------------------------------------------------------------------------------

    /**
     * #416 — Verifies that [MatchOperationRepositoryImpl.commitOperation] delegates to the
     * datasource with the same [MatchOperationCommit] argument.
     */
    @Test
    fun `#416 commitOperation delegates to datasource with the given commit`() = runTest {
        val commit =
            MatchOperationCommit(
                matchId = "match-1",
                operation = createOperation(id = "op-1", status = MatchOperationStatus.COMPLETED),
                playerTimes = emptyList(),
                lastCompletedOperationId = "op-1",
            )

        repository.commitOperation(commit)

        coVerify(exactly = 1) { matchOperationDataSource.commitOperation(commit) }
    }

    // -----------------------------------------------------------------------------------------
    // Regression tests — #416: markStaleOperationsAsFailed threshold filtering in data/core
    // -----------------------------------------------------------------------------------------

    /**
     * #416 — A fresh IN_PROGRESS operation (created 1 minute ago, below the 2-minute threshold)
     * must NOT be marked FAILED by the sweep.
     *
     * fakeNow = 10 min. Operation created at 9 min (1 min ago). Threshold = 2 min.
     * cutoff = 10 min - 2 min = 8 min. createdAt (9 min) >= cutoff (8 min) → not stale.
     */
    @Test
    fun `#416 markStaleOperationsAsFailed does not sweep operation created within the threshold`() = runTest {
        val matchId = "match-1"
        val twoMinutesMillis = 2 * 60 * 1000L
        val oneMinuteAgo = fakeNow - 1 * 60 * 1000L
        val freshOperation = createOperation(id = "op-fresh", createdAt = oneMinuteAgo)

        coEvery {
            matchOperationDataSource.getOperationsByMatchAndStatus(matchId, MatchOperationStatus.IN_PROGRESS)
        } returns listOf(freshOperation)

        repository.markStaleOperationsAsFailed(matchId, twoMinutesMillis)

        coVerify(exactly = 0) { matchOperationDataSource.updateOperation(any()) }
    }

    /**
     * #416 — A stale IN_PROGRESS operation (created 3 minutes ago, above the 2-minute threshold)
     * MUST be marked FAILED by the sweep.
     *
     * fakeNow = 10 min. Operation created at 7 min (3 min ago). Threshold = 2 min.
     * cutoff = 10 min - 2 min = 8 min. createdAt (7 min) < cutoff (8 min) → stale.
     */
    @Test
    fun `#416 markStaleOperationsAsFailed sweeps operation older than the threshold`() = runTest {
        val matchId = "match-1"
        val twoMinutesMillis = 2 * 60 * 1000L
        val threeMinutesAgo = fakeNow - 3 * 60 * 1000L
        val staleOperation = createOperation(id = "op-stale", createdAt = threeMinutesAgo)

        coEvery {
            matchOperationDataSource.getOperationsByMatchAndStatus(matchId, MatchOperationStatus.IN_PROGRESS)
        } returns listOf(staleOperation)

        repository.markStaleOperationsAsFailed(matchId, twoMinutesMillis)

        coVerify(exactly = 1) {
            matchOperationDataSource.updateOperation(staleOperation.copy(status = MatchOperationStatus.FAILED))
        }
    }

    /**
     * #416 — The sweep only transitions [MatchOperationStatus] to FAILED on the stale operation
     * itself. It never touches [Match.lastCompletedOperationId] or any other match field,
     * which would require a separate [MatchDataSource] that [MatchOperationRepositoryImpl]
     * does not hold.
     *
     * Verified by asserting the operation passed to [updateOperation] carries only the status
     * change, with all other fields identical to the original.
     */
    @Test
    fun `#416 markStaleOperationsAsFailed only changes status to FAILED and does not modify any match field`() = runTest {
        val matchId = "match-1"
        val twoMinutesMillis = 2 * 60 * 1000L
        val threeMinutesAgo = fakeNow - 3 * 60 * 1000L
        val staleOperation =
            createOperation(
                id = "op-stale-2",
                type = MatchOperationType.SUBSTITUTION,
                createdAt = threeMinutesAgo,
            )

        coEvery {
            matchOperationDataSource.getOperationsByMatchAndStatus(matchId, MatchOperationStatus.IN_PROGRESS)
        } returns listOf(staleOperation)

        repository.markStaleOperationsAsFailed(matchId, twoMinutesMillis)

        // The only change must be status = FAILED; every other field is preserved.
        val expectedUpdatedOperation = staleOperation.copy(status = MatchOperationStatus.FAILED)
        coVerify(exactly = 1) { matchOperationDataSource.updateOperation(expectedUpdatedOperation) }
        // No other interactions should have occurred (e.g. no match document update).
        coVerify(exactly = 0) { matchOperationDataSource.commitOperation(any()) }
        coVerify(exactly = 0) { matchOperationDataSource.createOperation(any()) }
    }
}
