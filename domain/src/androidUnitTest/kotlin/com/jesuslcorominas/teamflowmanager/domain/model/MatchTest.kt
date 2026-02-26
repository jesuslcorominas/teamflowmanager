package com.jesuslcorominas.teamflowmanager.domain.model

import org.junit.Assert.*
import org.junit.Test

class MatchTest {

    private fun createMatch(
        periodType: PeriodType = PeriodType.HALF_TIME,
        pauseCount: Int = 0,
        periods: List<MatchPeriod>? = null,
    ) = Match(
        teamName = "Team A",
        opponent = "Opponent",
        location = "Stadium",
        periodType = periodType,
        captainId = 0L,
        pauseCount = pauseCount,
        periods = periods ?: (1..periodType.numberOfPeriods).map {
            MatchPeriod(periodNumber = it, periodDuration = periodType.duration)
        },
    )

    @Test
    fun `canPause returns true when pauseCount is less than max pauses for HALF_TIME`() {
        val match = createMatch(periodType = PeriodType.HALF_TIME, pauseCount = 0)
        assertTrue(match.canPause())
    }

    @Test
    fun `canPause returns false when pauseCount equals max pauses for HALF_TIME`() {
        val match = createMatch(periodType = PeriodType.HALF_TIME, pauseCount = 1)
        assertFalse(match.canPause())
    }

    @Test
    fun `canPause returns false when pauseCount exceeds max pauses for HALF_TIME`() {
        val match = createMatch(periodType = PeriodType.HALF_TIME, pauseCount = 2)
        assertFalse(match.canPause())
    }

    @Test
    fun `canPause returns true when pauseCount is less than max pauses for QUARTER_TIME`() {
        val match = createMatch(periodType = PeriodType.QUARTER_TIME, pauseCount = 2)
        assertTrue(match.canPause())
    }

    @Test
    fun `canPause returns false when pauseCount equals max pauses for QUARTER_TIME`() {
        val match = createMatch(periodType = PeriodType.QUARTER_TIME, pauseCount = 3)
        assertFalse(match.canPause())
    }

    @Test
    fun `isLastPeriod returns false when last period has not started`() {
        val periods = listOf(
            MatchPeriod(periodNumber = 1, startTimeMillis = 0L, endTimeMillis = 0L),
            MatchPeriod(periodNumber = 2, startTimeMillis = 0L, endTimeMillis = 0L),
        )
        val match = createMatch(periods = periods)
        assertFalse(match.isLastPeriod())
    }

    @Test
    fun `isLastPeriod returns true when last period has started but not ended`() {
        val periods = listOf(
            MatchPeriod(periodNumber = 1, startTimeMillis = 1000L, endTimeMillis = 2000L),
            MatchPeriod(periodNumber = 2, startTimeMillis = 3000L, endTimeMillis = 0L),
        )
        val match = createMatch(periods = periods)
        assertTrue(match.isLastPeriod())
    }

    @Test
    fun `isLastPeriod returns false when last period has already ended`() {
        val periods = listOf(
            MatchPeriod(periodNumber = 1, startTimeMillis = 1000L, endTimeMillis = 2000L),
            MatchPeriod(periodNumber = 2, startTimeMillis = 3000L, endTimeMillis = 4000L),
        )
        val match = createMatch(periods = periods)
        assertFalse(match.isLastPeriod())
    }

    @Test
    fun `getTotalElapsed returns sum of elapsed time for completed periods`() {
        val currentTime = 5000L
        val periods = listOf(
            MatchPeriod(periodNumber = 1, periodDuration = 3000L, startTimeMillis = 1000L, endTimeMillis = 2000L),
            MatchPeriod(periodNumber = 2, periodDuration = 3000L, startTimeMillis = 3000L, endTimeMillis = 0L),
        )
        val match = createMatch(periods = periods)
        // Period 1: ended, elapsed = 2000 - 1000 = 1000
        // Period 2: ongoing, elapsed = 5000 - 3000 = 2000
        assertEquals(3000L, match.getTotalElapsed(currentTime))
    }

    @Test
    fun `isInProgress returns true when status is IN_PROGRESS`() {
        val match = createMatch().copy(status = MatchStatus.IN_PROGRESS)
        assertTrue(match.isInProgress)
    }

    @Test
    fun `isInProgress returns false when status is not IN_PROGRESS`() {
        val match = createMatch().copy(status = MatchStatus.SCHEDULED)
        assertFalse(match.isInProgress)
    }

    @Test
    fun `isStarted returns true when status is IN_PROGRESS`() {
        val match = createMatch().copy(status = MatchStatus.IN_PROGRESS)
        assertTrue(match.isStarted)
    }

    @Test
    fun `isStarted returns true when status is PAUSED`() {
        val match = createMatch().copy(status = MatchStatus.PAUSED)
        assertTrue(match.isStarted)
    }

    @Test
    fun `isStarted returns true when status is TIMEOUT`() {
        val match = createMatch().copy(status = MatchStatus.TIMEOUT)
        assertTrue(match.isStarted)
    }

    @Test
    fun `isStarted returns false when status is SCHEDULED`() {
        val match = createMatch().copy(status = MatchStatus.SCHEDULED)
        assertFalse(match.isStarted)
    }
}
