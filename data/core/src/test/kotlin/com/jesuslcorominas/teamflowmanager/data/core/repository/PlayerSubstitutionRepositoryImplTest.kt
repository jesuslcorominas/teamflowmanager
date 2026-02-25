package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlayerSubstitutionRepositoryImplTest {

    private lateinit var playerSubstitutionDataSource: PlayerSubstitutionDataSource
    private lateinit var repository: PlayerSubstitutionRepositoryImpl

    @Before
    fun setup() {
        playerSubstitutionDataSource = mockk(relaxed = true)
        repository = PlayerSubstitutionRepositoryImpl(playerSubstitutionDataSource)
    }

    private fun createSubstitution(
        id: Long = 0L,
        matchId: Long = 1L,
        playerOutId: Long = 10L,
        playerInId: Long = 11L,
        operationId: String? = null,
    ) = PlayerSubstitution(
        id = id,
        matchId = matchId,
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = 30_000L,
        matchElapsedTimeMillis = 30_000L,
        operationId = operationId,
    )

    // --- getMatchSubstitutions ---

    @Test
    fun `givenMatchWithSubstitutions_whenGetMatchSubstitutions_thenDelegatesToDataSource`() = runTest {
        val matchId = 1L
        val substitutions = listOf(
            createSubstitution(id = 1L, matchId = matchId),
            createSubstitution(id = 2L, matchId = matchId, playerOutId = 20L, playerInId = 21L),
        )
        every { playerSubstitutionDataSource.getMatchSubstitutions(matchId) } returns flowOf(substitutions)

        val result = repository.getMatchSubstitutions(matchId).first()

        assertEquals(substitutions, result)
    }

    @Test
    fun `givenMatchWithNoSubstitutions_whenGetMatchSubstitutions_thenReturnsEmptyList`() = runTest {
        val matchId = 99L
        every { playerSubstitutionDataSource.getMatchSubstitutions(matchId) } returns flowOf(emptyList())

        val result = repository.getMatchSubstitutions(matchId).first()

        assertEquals(emptyList<PlayerSubstitution>(), result)
    }

    // --- insertSubstitution ---

    @Test
    fun `givenSubstitution_whenInsertSubstitution_thenReturnsInsertedId`() = runTest {
        val substitution = createSubstitution()
        coEvery { playerSubstitutionDataSource.insertSubstitution(substitution) } returns 3L

        val result = repository.insertSubstitution(substitution)

        assertEquals(3L, result)
        coVerify { playerSubstitutionDataSource.insertSubstitution(substitution) }
    }

    @Test
    fun `givenSubstitutionWithOperationId_whenInsertSubstitution_thenDelegatesToDataSource`() = runTest {
        val substitution = createSubstitution(operationId = "op-sub-123")
        coEvery { playerSubstitutionDataSource.insertSubstitution(substitution) } returns 4L

        val result = repository.insertSubstitution(substitution)

        assertEquals(4L, result)
        coVerify { playerSubstitutionDataSource.insertSubstitution(substitution) }
    }
}
