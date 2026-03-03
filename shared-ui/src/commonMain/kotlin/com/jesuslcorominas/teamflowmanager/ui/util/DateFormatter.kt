package com.jesuslcorominas.teamflowmanager.ui.util

expect object DateFormatter {
    fun formatDate(timestamp: Long): String
    fun formatTime(timestamp: Long): String
    fun formatDateTime(timestamp: Long): String
    fun formatTimeOfDay(timeOfDayMillis: Long): String
}
