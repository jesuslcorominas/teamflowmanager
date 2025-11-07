package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
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
            Match(
                id = 1L,
                dateTime = 1000L,
                captainId = 10L,
                teamName = "Team B",
                opponent = "Team C",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                periods = listOf(
                    MatchPeriod(1, startTimeMillis = 1000L, endTimeMillis = 2000L, periodDuration = PeriodType.HALF_TIME.duration)
                )
            ),
            Match(
                id = 2L,
                dateTime = 2000L,
                captainId = 20L,
                teamName = "Team B",
                opponent = "Team D",
                location = "Stadium B",
                periodType = PeriodType.HALF_TIME,
                periods = listOf(
                    MatchPeriod(1, startTimeMillis = 2000L, endTimeMillis = 3000L, periodDuration = PeriodType.HALF_TIME.duration)
                )
            ),
            Match(
                id = 3L,
                dateTime = 3000L,
                captainId = 30L,
                teamName = "Team B",
                opponent = "Team E",
                location = "Stadium C",
                periodType = PeriodType.HALF_TIME,
                periods = listOf(
                    MatchPeriod(1, startTimeMillis = 3000L, endTimeMillis = 4000L, periodDuration = PeriodType.HALF_TIME.duration)
                )
            ),
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
            Match(
                id = 1L,
                dateTime = 1000L,
                captainId = 10L,
                teamName = "Team B",
                opponent = "Team C",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                periods = listOf(
                    MatchPeriod(1, periodDuration = PeriodType.HALF_TIME.duration) // No time elapsed
                )
            ),
            Match(
                id = 2L,
                dateTime = 2000L,
                captainId = 20L,
                teamName = "Team B",
                opponent = "Team D",
                location = "Stadium B",
                periodType = PeriodType.HALF_TIME,
                periods = listOf(
                    MatchPeriod(1, startTimeMillis = 2000L, endTimeMillis = 3000L, periodDuration = PeriodType.HALF_TIME.duration)
                )
            ),
            Match(
                id = 3L,
                dateTime = 3000L,
                captainId = 30L,
                teamName = "Team B",
                opponent = "Team E",
                location = "Stadium C",
                periodType = PeriodType.HALF_TIME,
                periods = listOf(
                    MatchPeriod(1, periodDuration = PeriodType.HALF_TIME.duration) // No time elapsed
                )
            ),
        )
        coEvery { matchRepository.getAllMatches() } returns flowOf(matches)

        // When
        val result = useCase.invoke(2)

        // Then
        assertEquals(1, result.size)
        assertEquals(20L, result[0])
    }
}
