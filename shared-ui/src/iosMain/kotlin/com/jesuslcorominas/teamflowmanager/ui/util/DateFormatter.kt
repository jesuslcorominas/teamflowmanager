package com.jesuslcorominas.teamflowmanager.ui.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970

actual object DateFormatter {
    private fun formatter(format: String): NSDateFormatter = NSDateFormatter().apply {
        dateFormat = format
        locale = NSLocale.currentLocale
    }

    actual fun formatDate(timestamp: Long): String =
        formatter("dd/MM/yyyy").stringFromDate(NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0))

    actual fun formatTime(timestamp: Long): String =
        formatter("HH:mm").stringFromDate(NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0))

    actual fun formatDateTime(timestamp: Long): String =
        formatter("dd/MM/yyyy HH:mm").stringFromDate(NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0))

    actual fun formatTimeOfDay(timeOfDayMillis: Long): String {
        val hours = (timeOfDayMillis / (60 * 60 * 1000)) % 24
        val minutes = (timeOfDayMillis / (60 * 1000)) % 60
        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    }
}
