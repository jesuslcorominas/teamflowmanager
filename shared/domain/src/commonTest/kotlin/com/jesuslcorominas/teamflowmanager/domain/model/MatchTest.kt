package com.jesuslcorominas.teamflowmanager.domain.model

import org.junit.Assert.*
import org.junit.Test

class MatchTest {

    @Test
    fun `getPeriodDurationMillis returns 25 minutes for 2 periods`() {
        // Given
        val match = Match(numberOfPeriods = 2, teamName = "Team A")

        // When
        val duration = match.getPeriodDurationMillis()

        // Then
        assertEquals(25 * 60 * 1000L, duration)
    }

    @Test
    fun `getPeriodDurationMillis returns 12 minutes 30 seconds for 4 periods`() {
        // Given
        val match = Match(numberOfPeriods = 4, teamName = "Team A")

        // When
        val duration = match.getPeriodDurationMillis()

        // Then
        assertEquals((12 * 60 + 30) * 1000L, duration)
    }

    @Test
    fun `getMaxPauses returns 1 for 2 periods`() {
        // Given
        val match = Match(numberOfPeriods = 2, teamName = "Team A")

        // When
        val maxPauses = match.getMaxPauses()

        // Then
        assertEquals(1, maxPauses)
    }

    @Test
    fun `getMaxPauses returns 3 for 4 periods`() {
        // Given
        val match = Match(numberOfPeriods = 4, teamName = "Team A")

        // When
        val maxPauses = match.getMaxPauses()

        // Then
        assertEquals(3, maxPauses)
    }

    @Test
    fun `canPause returns true when pause count is less than max`() {
        // Given
        val match = Match(numberOfPeriods = 2, pauseCount = 0, teamName = "Team A")

        // When
        val canPause = match.canPause()

        // Then
        assertTrue(canPause)
    }

    @Test
    fun `canPause returns false when pause count equals max`() {
        // Given
        val match = Match(numberOfPeriods = 2, pauseCount = 1, teamName = "Team A")

        // When
        val canPause = match.canPause()

        // Then
        assertFalse(canPause)
    }

    @Test
    fun `canPause returns false when pause count exceeds max`() {
        // Given
        val match = Match(numberOfPeriods = 2, pauseCount = 2, teamName = "Team A")

        // When
        val canPause = match.canPause()

        // Then
        assertFalse(canPause)
    }

    @Test
    fun `isLastPeriod returns false when current period is less than total`() {
        // Given
        val match = Match(numberOfPeriods = 2, currentPeriod = 1, teamName = "Team A")

        // When
        val isLast = match.isLastPeriod()

        // Then
        assertFalse(isLast)
    }

    @Test
    fun `isLastPeriod returns true when current period equals total`() {
        // Given
        val match = Match(numberOfPeriods = 2, currentPeriod = 2, teamName = "Team A")

        // When
        val isLast = match.isLastPeriod()

        // Then
        assertTrue(isLast)
    }

    @Test
    fun `isLastPeriod returns true when current period exceeds total`() {
        // Given
        val match = Match(numberOfPeriods = 2, currentPeriod = 3, teamName = "Team A")

        // When
        val isLast = match.isLastPeriod()

        // Then
        assertTrue(isLast)
    }
}
