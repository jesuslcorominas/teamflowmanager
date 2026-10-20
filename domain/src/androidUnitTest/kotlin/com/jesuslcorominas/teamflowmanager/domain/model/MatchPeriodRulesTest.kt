package com.jesuslcorominas.teamflowmanager.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchPeriodRulesTest {
    @Test
    fun `isPeriodTypeSelectable is true only for FOOTBALL_5`() {
        // Then
        assertTrue(MatchPeriodRules.isPeriodTypeSelectable(TeamType.FOOTBALL_5))
        assertFalse(MatchPeriodRules.isPeriodTypeSelectable(TeamType.FOOTBALL_7))
        assertFalse(MatchPeriodRules.isPeriodTypeSelectable(TeamType.FOOTBALL_8))
        assertFalse(MatchPeriodRules.isPeriodTypeSelectable(TeamType.FOOTBALL_11))
    }

    @Test
    fun `periodDuration for FOOTBALL_5 HALF_TIME is 25 minutes`() {
        // When
        val duration = MatchPeriodRules.periodDuration(TeamType.FOOTBALL_5, PeriodType.HALF_TIME)

        // Then
        assertEquals(25 * 60 * 1000L, duration)
    }

    @Test
    fun `periodDuration for FOOTBALL_5 QUARTER_TIME is 12 minutes 30 seconds`() {
        // When
        val duration = MatchPeriodRules.periodDuration(TeamType.FOOTBALL_5, PeriodType.QUARTER_TIME)

        // Then
        assertEquals(750_000L, duration)
    }

    @Test
    fun `periodDuration for FOOTBALL_7 and FOOTBALL_8 is 30 minutes`() {
        // Then
        assertEquals(1_800_000L, MatchPeriodRules.periodDuration(TeamType.FOOTBALL_7, PeriodType.HALF_TIME))
        assertEquals(1_800_000L, MatchPeriodRules.periodDuration(TeamType.FOOTBALL_8, PeriodType.HALF_TIME))
    }

    @Test
    fun `periodDuration for FOOTBALL_11 is 45 minutes`() {
        // When
        val duration = MatchPeriodRules.periodDuration(TeamType.FOOTBALL_11, PeriodType.HALF_TIME)

        // Then
        assertEquals(2_700_000L, duration)
    }

    @Test
    fun `periodDuration for FOOTBALL_7, 8 and 11 ignores the received period type`() {
        // Then
        assertEquals(1_800_000L, MatchPeriodRules.periodDuration(TeamType.FOOTBALL_7, PeriodType.QUARTER_TIME))
        assertEquals(1_800_000L, MatchPeriodRules.periodDuration(TeamType.FOOTBALL_8, PeriodType.QUARTER_TIME))
        assertEquals(2_700_000L, MatchPeriodRules.periodDuration(TeamType.FOOTBALL_11, PeriodType.QUARTER_TIME))
    }

    @Test
    fun `resolvePeriodType returns the requested type for FOOTBALL_5`() {
        // Then
        assertEquals(PeriodType.HALF_TIME, MatchPeriodRules.resolvePeriodType(TeamType.FOOTBALL_5, PeriodType.HALF_TIME))
        assertEquals(PeriodType.QUARTER_TIME, MatchPeriodRules.resolvePeriodType(TeamType.FOOTBALL_5, PeriodType.QUARTER_TIME))
    }

    @Test
    fun `resolvePeriodType forces HALF_TIME for FOOTBALL_7, 8 and 11`() {
        listOf(TeamType.FOOTBALL_7, TeamType.FOOTBALL_8, TeamType.FOOTBALL_11).forEach { teamType ->
            // Then
            assertEquals(PeriodType.HALF_TIME, MatchPeriodRules.resolvePeriodType(teamType, PeriodType.QUARTER_TIME))
        }
    }

    @Test
    fun `buildPeriods for FOOTBALL_5 QUARTER_TIME returns 4 numbered periods of 12 minutes 30 seconds`() {
        // When
        val periods = MatchPeriodRules.buildPeriods(TeamType.FOOTBALL_5, PeriodType.QUARTER_TIME)

        // Then
        assertEquals(listOf(1, 2, 3, 4), periods.map { it.periodNumber })
        assertTrue(periods.all { it.periodDuration == 750_000L })
    }

    @Test
    fun `buildPeriods for FOOTBALL_7 returns 2 periods of 30 minutes`() {
        // When
        val periods = MatchPeriodRules.buildPeriods(TeamType.FOOTBALL_7, PeriodType.HALF_TIME)

        // Then
        assertEquals(2, periods.size)
        assertTrue(periods.all { it.periodDuration == 1_800_000L })
    }

    @Test
    fun `buildPeriods for FOOTBALL_11 returns 2 periods of 45 minutes`() {
        // When
        val periods = MatchPeriodRules.buildPeriods(TeamType.FOOTBALL_11, PeriodType.HALF_TIME)

        // Then
        assertEquals(2, periods.size)
        assertTrue(periods.all { it.periodDuration == 2_700_000L })
    }

    @Test
    fun `buildPeriods for FOOTBALL_11 requesting QUARTER_TIME still returns 2 periods of 45 minutes`() {
        // When
        val periods = MatchPeriodRules.buildPeriods(TeamType.FOOTBALL_11, PeriodType.QUARTER_TIME)

        // Then
        assertEquals(2, periods.size)
        assertTrue(periods.all { it.periodDuration == 2_700_000L })
    }
}
