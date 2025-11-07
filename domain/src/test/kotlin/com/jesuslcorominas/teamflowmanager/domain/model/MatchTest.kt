package com.jesuslcorominas.teamflowmanager.domain.model

import org.junit.Assert.*
import org.junit.Test

class MatchTest {

    @Test
    fun `period duration is 25 minutes for HALF_TIME (2 periods)`() {
        // Given
        val match = Match(
            teamName = "Team A",
            opponent = "Team B",
            location = "Stadium A",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L
        )

        // When
        val duration = match.periodType.duration

        // Then
        assertEquals(25 * 60 * 1000L, duration)
    }

    @Test
    fun `period duration is 12 minutes 30 seconds for QUARTER_TIME (4 periods)`() {
        // Given
        val match = Match(
            teamName = "Team A",
            opponent = "Team B",
            location = "Stadium A",
            periodType = PeriodType.QUARTER_TIME,
            captainId = 1L
        )

        // When
        val duration = match.periodType.duration

        // Then
        assertEquals((12 * 60 + 30) * 1000L, duration)
    }

    @Test
    fun `canPause returns true when pause count is less than max for HALF_TIME`() {
        // Given
        val match = Match(
            teamName = "Team A",
            opponent = "Team B",
            location = "Stadium A",
            periodType = PeriodType.HALF_TIME,
            pauseCount = 0,
            captainId = 1L
        )

        // When
        val canPause = match.canPause()

        // Then
        assertTrue(canPause) // maxPauses for HALF_TIME (2 periods) is 1
    }

    @Test
    fun `canPause returns false when pause count equals max for HALF_TIME`() {
        // Given
        val match = Match(
            teamName = "Team A",
            opponent = "Team B",
            location = "Stadium A",
            periodType = PeriodType.HALF_TIME,
            pauseCount = 1,
            captainId = 1L
        )

        // When
        val canPause = match.canPause()

        // Then
        assertFalse(canPause) // maxPauses for HALF_TIME (2 periods) is 1
    }

    @Test
    fun `canPause returns false when pause count exceeds max for HALF_TIME`() {
        // Given
        val match = Match(
            teamName = "Team A",
            opponent = "Team B",
            location = "Stadium A",
            periodType = PeriodType.HALF_TIME,
            pauseCount = 2,
            captainId = 1L
        )

        // When
        val canPause = match.canPause()

        // Then
        assertFalse(canPause)
    }

    @Test
    fun `canPause returns true for QUARTER_TIME when pause count is less than max`() {
        // Given
        val match = Match(
            teamName = "Team A",
            opponent = "Team B",
            location = "Stadium A",
            periodType = PeriodType.QUARTER_TIME,
            pauseCount = 2,
            captainId = 1L
        )

        // When
        val canPause = match.canPause()

        // Then
        assertTrue(canPause) // maxPauses for QUARTER_TIME (4 periods) is 3
    }

    @Test
    fun `canPause returns false for QUARTER_TIME when pause count equals max`() {
        // Given
        val match = Match(
            teamName = "Team A",
            opponent = "Team B",
            location = "Stadium A",
            periodType = PeriodType.QUARTER_TIME,
            pauseCount = 3,
            captainId = 1L
        )

        // When
        val canPause = match.canPause()

        // Then
        assertFalse(canPause) // maxPauses for QUARTER_TIME (4 periods) is 3
    }

    @Test
    fun `isLastPeriod returns false when last period has not started`() {
        // Given
        val match = Match(
            teamName = "Team A",
            opponent = "Team B",
            location = "Stadium A",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
            periods = listOf(
                MatchPeriod(periodNumber = 1, startTimeMillis = 1000L, endTimeMillis = 2000L),
                MatchPeriod(periodNumber = 2, startTimeMillis = 0L, endTimeMillis = 0L)
            )
        )

        // When
        val isLast = match.isLastPeriod()

        // Then
        assertFalse(isLast)
    }

    @Test
    fun `isLastPeriod returns true when last period has started but not ended`() {
        // Given
        val match = Match(
            teamName = "Team A",
            opponent = "Team B",
            location = "Stadium A",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
            periods = listOf(
                MatchPeriod(periodNumber = 1, startTimeMillis = 1000L, endTimeMillis = 2000L),
                MatchPeriod(periodNumber = 2, startTimeMillis = 3000L, endTimeMillis = 0L)
            )
        )

        // When
        val isLast = match.isLastPeriod()

        // Then
        assertTrue(isLast)
    }

    @Test
    fun `isLastPeriod returns false when last period has ended`() {
        // Given
        val match = Match(
            teamName = "Team A",
            opponent = "Team B",
            location = "Stadium A",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
            periods = listOf(
                MatchPeriod(periodNumber = 1, startTimeMillis = 1000L, endTimeMillis = 2000L),
                MatchPeriod(periodNumber = 2, startTimeMillis = 3000L, endTimeMillis = 4000L)
            )
        )

        // When
        val isLast = match.isLastPeriod()

        // Then
        assertFalse(isLast)
    }
}
