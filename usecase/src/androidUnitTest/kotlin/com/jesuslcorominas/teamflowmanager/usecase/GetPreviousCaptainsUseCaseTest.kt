package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPreviousCaptainsUseCase
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
    fun `invoke should return captain IDs from last 2 matches sorted by dateTime descending`() = runTest {
        // Given - matches with played periods, ordered by dateTime desc
        val matches = listOf(
            Match(
                id = 1L, dateTime = 1000L, captainId = 10L, teamName = "Team B",
                opponent = "A", location = "S", periodType = PeriodType.HALF_TIME,
                periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 100L, endTimeMillis = 200L)),
            ),
            Match(
                id = 2L, dateTime = 2000L, captainId = 20L, teamName = "Team B",
                opponent = "A", location = "S", periodType = PeriodType.HALF_TIME,
                periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 100L, endTimeMillis = 200L)),
            ),
            Match(
                id = 3L, dateTime = 3000L, captainId = 30L, teamName = "Team B",
                opponent = "A", location = "S", periodType = PeriodType.HALF_TIME,
                periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 100L, endTimeMillis = 200L)),
            ),
        )
        coEvery { matchRepository.getAllMatches() } returns flowOf(matches)

        // When
        val result = useCase.invoke(2)

        // Then - most recent first
        assertEquals(2, result.size)
        assertEquals(30L, result[0]) // Most recent (dateTime=3000)
        assertEquals(20L, result[1])
    }

    @Test
    fun `invoke should filter out matches without played periods`() = runTest {
        // Given
        val matches = listOf(
            // Not played: period startTimeMillis = 0 means not started
            Match(
                id = 1L, dateTime = 1000L, captainId = 0L, teamName = "Team B",
                opponent = "A", location = "S", periodType = PeriodType.HALF_TIME,
                periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L)),
            ),
            // Played: period has start and end times
            Match(
                id = 2L, dateTime = 2000L, captainId = 20L, teamName = "Team B",
                opponent = "A", location = "S", periodType = PeriodType.HALF_TIME,
                periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 100L, endTimeMillis = 200L)),
            ),
            // Not played: period startTimeMillis = 0
            Match(
                id = 3L, dateTime = 3000L, captainId = 0L, teamName = "Team B",
                opponent = "A", location = "S", periodType = PeriodType.HALF_TIME,
                periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 0L, endTimeMillis = 0L)),
            ),
        )
        coEvery { matchRepository.getAllMatches() } returns flowOf(matches)

        // When
        val result = useCase.invoke(2)

        // Then - only the played match is included
        assertEquals(1, result.size)
        assertEquals(20L, result[0])
    }

    @Test
    fun `invoke should include zero captainId for matches that have been played without captain`() = runTest {
        // Given
        val matches = listOf(
            Match(
                id = 1L, dateTime = 1000L, captainId = 0L, teamName = "Team B",
                opponent = "A", location = "S", periodType = PeriodType.HALF_TIME,
                periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 100L, endTimeMillis = 200L)),
            ),
            Match(
                id = 2L, dateTime = 2000L, captainId = 20L, teamName = "Team B",
                opponent = "A", location = "S", periodType = PeriodType.HALF_TIME,
                periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L, startTimeMillis = 100L, endTimeMillis = 200L)),
            ),
        )
        coEvery { matchRepository.getAllMatches() } returns flowOf(matches)

        // When
        val result = useCase.invoke(2)

        // Then - most recent first, 0L (no captain assigned) is included
        assertEquals(2, result.size)
        assertEquals(20L, result[0])
        assertEquals(0L, result[1])
    }
}
