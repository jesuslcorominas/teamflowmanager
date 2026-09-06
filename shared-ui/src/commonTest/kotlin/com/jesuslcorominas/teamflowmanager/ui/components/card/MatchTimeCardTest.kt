package com.jesuslcorominas.teamflowmanager.ui.components.card

import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import kotlin.test.Test
import kotlin.test.assertEquals

class MatchTimeCardTest {
    @Test
    fun `well-formed period returns actual elapsed time between start and end`() {
        val period =
            MatchPeriod(
                periodNumber = 1,
                periodDuration = 1_500_000L,
                startTimeMillis = 1_000_000L,
                endTimeMillis = 2_600_000L,
            )

        assertEquals(1_600_000L, calculateFinishedPeriodElapsedTime(period))
    }

    @Test
    fun `finished period with zero timestamps falls back to configured period duration`() {
        val period =
            MatchPeriod(
                periodNumber = 1,
                periodDuration = 1_500_000L,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
            )

        assertEquals(1_500_000L, calculateFinishedPeriodElapsedTime(period))
    }

    @Test
    fun `finished period with only start timestamp zero falls back to configured duration`() {
        val period =
            MatchPeriod(
                periodNumber = 1,
                periodDuration = 750_000L,
                startTimeMillis = 0L,
                endTimeMillis = 2_000_000L,
            )

        assertEquals(750_000L, calculateFinishedPeriodElapsedTime(period))
    }

    @Test
    fun `period started but never ended falls back to configured duration`() {
        val period =
            MatchPeriod(
                periodNumber = 1,
                periodDuration = 1_500_000L,
                startTimeMillis = 1_000_000L,
                endTimeMillis = 0L,
            )

        assertEquals(1_500_000L, calculateFinishedPeriodElapsedTime(period))
    }

    @Test
    fun `inverted timestamps are clamped to zero instead of returning a negative value`() {
        val period =
            MatchPeriod(
                periodNumber = 1,
                periodDuration = 1_500_000L,
                startTimeMillis = 2_600_000L,
                endTimeMillis = 1_000_000L,
            )

        assertEquals(0L, calculateFinishedPeriodElapsedTime(period))
    }
}
