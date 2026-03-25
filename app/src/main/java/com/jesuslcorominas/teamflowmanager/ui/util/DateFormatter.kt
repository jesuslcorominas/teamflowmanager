package com.jesuslcorominas.teamflowmanager.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    /**
     * Formats time of day from milliseconds since midnight (00:00:00).
     * This is used for match time fields that store only the time component.
     *
     * @param timeOfDayMillis milliseconds since midnight (0 = 00:00, 3600000 = 01:00, etc.)
     * @return formatted time string in HH:mm format (e.g., "00:00", "14:30")
     */
    fun formatTimeOfDay(timeOfDayMillis: Long): String {
        val hours = (timeOfDayMillis / (60 * 60 * 1000)) % 24
        val minutes = (timeOfDayMillis / (60 * 1000)) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }
}
