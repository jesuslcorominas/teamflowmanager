package com.jesuslcorominas.teamflowmanager.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormatterTest {

    @Test
    fun `formatTimeOfDay should format 00-00 correctly`() {
        // Given - 00:00 (midnight)
        val timeMillis = 0L

        // When
        val result = DateFormatter.formatTimeOfDay(timeMillis)

        // Then
        assertEquals("00:00", result)
    }

    @Test
    fun `formatTimeOfDay should format 01-00 correctly`() {
        // Given - 01:00 (1 hour = 3600000 milliseconds)
        val timeMillis = 1 * 60 * 60 * 1000L

        // When
        val result = DateFormatter.formatTimeOfDay(timeMillis)

        // Then
        assertEquals("01:00", result)
    }

    @Test
    fun `formatTimeOfDay should format 14-30 correctly`() {
        // Given - 14:30 (14 hours + 30 minutes)
        val timeMillis = (14 * 60 * 60 * 1000) + (30 * 60 * 1000)

        // When
        val result = DateFormatter.formatTimeOfDay(timeMillis)

        // Then
        assertEquals("14:30", result)
    }

    @Test
    fun `formatTimeOfDay should format 23-59 correctly`() {
        // Given - 23:59 (23 hours + 59 minutes)
        val timeMillis = (23 * 60 * 60 * 1000) + (59 * 60 * 1000)

        // When
        val result = DateFormatter.formatTimeOfDay(timeMillis)

        // Then
        assertEquals("23:59", result)
    }

    @Test
    fun `formatTimeOfDay should format 12-00 correctly`() {
        // Given - 12:00 (noon)
        val timeMillis = 12 * 60 * 60 * 1000L

        // When
        val result = DateFormatter.formatTimeOfDay(timeMillis)

        // Then
        assertEquals("12:00", result)
    }

    @Test
    fun `formatTimeOfDay should format 09-05 with leading zeros`() {
        // Given - 09:05
        val timeMillis = (9 * 60 * 60 * 1000) + (5 * 60 * 1000)

        // When
        val result = DateFormatter.formatTimeOfDay(timeMillis)

        // Then
        assertEquals("09:05", result)
    }
}
