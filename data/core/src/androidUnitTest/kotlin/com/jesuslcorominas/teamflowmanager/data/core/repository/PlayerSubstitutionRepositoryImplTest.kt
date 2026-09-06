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
        id: String = "",
        matchId: String = "1",
        playerOutId: String = "10",
        playerInId: String = "11",
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
        val matchId = "1"
        val substitutions = listOf(
            createSubstitution(id = "1", matchId = matchId),
            createSubstitution(id = "2", matchId = matchId, playerOutId = "20", playerInId = "21"),
        )
        every { playerSubstitutionDataSource.getMatchSubstitutions(matchId) } returns flowOf(substitutions)

        val result = repository.getMatchSubstitutions(matchId).first()

        assertEquals(substitutions, result)
    }

    @Test
    fun `givenMatchWithNoSubstitutions_whenGetMatchSubstitutions_thenReturnsEmptyList`() = runTest {
        val matchId = "99"
        every { playerSubstitutionDataSource.getMatchSubstitutions(matchId) } returns flowOf(emptyList())

        val result = repository.getMatchSubstitutions(matchId).first()

        assertEquals(emptyList<PlayerSubstitution>(), result)
    }

    // --- insertSubstitution ---

    @Test
    fun `givenSubstitution_whenInsertSubstitution_thenReturnsInsertedId`() = runTest {
        val substitution = createSubstitution()
        coEvery { playerSubstitutionDataSource.insertSubstitution(substitution) } returns "sub-3"

        val result = repository.insertSubstitution(substitution)

        assertEquals("sub-3", result)
        coVerify { playerSubstitutionDataSource.insertSubstitution(substitution) }
    }

    @Test
    fun `givenSubstitutionWithOperationId_whenInsertSubstitution_thenDelegatesToDataSource`() = runTest {
        val substitution = createSubstitution(operationId = "op-sub-123")
        coEvery { playerSubstitutionDataSource.insertSubstitution(substitution) } returns "sub-4"

        val result = repository.insertSubstitution(substitution)

        assertEquals("sub-4", result)
        coVerify { playerSubstitutionDataSource.insertSubstitution(substitution) }
    }
}
