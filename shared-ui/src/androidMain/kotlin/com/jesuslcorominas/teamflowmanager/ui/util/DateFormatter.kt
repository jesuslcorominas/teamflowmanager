package com.jesuslcorominas.teamflowmanager.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual object DateFormatter {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    actual fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    actual fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    actual fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    actual fun formatTimeOfDay(timeOfDayMillis: Long): String {
        val hours = (timeOfDayMillis / (60 * 60 * 1000)) % 24
        val minutes = (timeOfDayMillis / (60 * 1000)) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }
}
