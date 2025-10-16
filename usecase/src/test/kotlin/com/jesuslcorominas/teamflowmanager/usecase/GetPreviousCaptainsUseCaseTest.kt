package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetPreviousCaptainsUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var useCase: GetPreviousCaptainsUseCase

    @Before
    fun setup() {
        matchRepository = mockk()
        useCase = GetPreviousCaptainsUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should return empty list when no matches exist`() = runTest {
        // Given
        coEvery { matchRepository.getAllMatches() } returns flowOf(emptyList())

        // When
        val result = useCase.invoke()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke should return captain IDs from last 2 matches`() = runTest {
        // Given
        val matches = listOf(
            Match(id = 1L, date = 1000L, elapsedTimeMillis = 100L, captainId = 10L),
            Match(id = 2L, date = 2000L, elapsedTimeMillis = 100L, captainId = 20L),
            Match(id = 3L, date = 3000L, elapsedTimeMillis = 100L, captainId = 30L),
        )
        coEvery { matchRepository.getAllMatches() } returns flowOf(matches)

        // When
        val result = useCase.invoke(2)

        // Then
        assertEquals(2, result.size)
        assertEquals(30L, result[0]) // Most recent
        assertEquals(20L, result[1])
    }

    @Test
    fun `invoke should filter out matches without elapsed time`() = runTest {
        // Given
        val matches = listOf(
            Match(id = 1L, date = 1000L, elapsedTimeMillis = 0L, captainId = null),
            Match(id = 2L, date = 2000L, elapsedTimeMillis = 100L, captainId = 20L),
            Match(id = 3L, date = 3000L, elapsedTimeMillis = 0L, captainId = null),
        )
        coEvery { matchRepository.getAllMatches() } returns flowOf(matches)

        // When
        val result = useCase.invoke(2)

        // Then
        assertEquals(1, result.size)
        assertEquals(20L, result[0])
    }

    @Test
    fun `invoke should return null for matches without captain`() = runTest {
        // Given
        val matches = listOf(
            Match(id = 1L, date = 1000L, elapsedTimeMillis = 100L, captainId = null),
            Match(id = 2L, date = 2000L, elapsedTimeMillis = 100L, captainId = 20L),
        )
        coEvery { matchRepository.getAllMatches() } returns flowOf(matches)

        // When
        val result = useCase.invoke(2)

        // Then
        assertEquals(2, result.size)
        assertEquals(20L, result[0])
        assertEquals(null, result[1])
    }
}
