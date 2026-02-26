package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchOperationDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
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
    private lateinit var repository: MatchOperationRepositoryImpl

    @Before
    fun setup() {
        matchOperationDataSource = mockk(relaxed = true)
        repository = MatchOperationRepositoryImpl(matchOperationDataSource)
    }

    private fun createOperation(
        id: String = "",
        matchId: Long = 1L,
        teamId: Long = 1L,
        type: MatchOperationType = MatchOperationType.START,
        status: MatchOperationStatus = MatchOperationStatus.IN_PROGRESS,
    ) = MatchOperation(
        id = id,
        matchId = matchId,
        teamId = teamId,
        type = type,
        status = status,
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
}
