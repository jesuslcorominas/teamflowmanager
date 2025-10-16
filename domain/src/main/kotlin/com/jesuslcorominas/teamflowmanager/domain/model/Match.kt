package com.jesuslcorominas.teamflowmanager.domain.model

data class Match(
    val id: Long = 0L,
    val teamId: Long = 1L,
    val opponent: String? = null,
    val location: String? = null,
    val date: Long? = null, // Date in milliseconds
    val time: Long? = null, // Time in milliseconds (hours and minutes of day)
    val numberOfPeriods: Int = 2, // Number of periods: 2 for halves, 4 for quarters
    val squadCallUpIds: List<Long> = emptyList(), // Players selected for match squad (convocatoria)
    val captainId: Long? = null, // Captain for this match
    val startingLineupIds: List<Long> = emptyList(),
    val substituteIds: List<Long> = emptyList(),
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val archived: Boolean = false,
    val currentPeriod: Int = 1, // Current period (1-based index)
    val pauseCount: Int = 0, // Number of times the match has been paused
) {
    /**
     * Get the duration of each period in milliseconds
     * 2 periods = 25 minutes each = 1,500,000 ms
     * 4 periods = 12.5 minutes each = 750,000 ms
     */
    fun getPeriodDurationMillis(): Long {
        return if (numberOfPeriods == 2) {
            25 * 60 * 1000L // 25 minutes
        } else {
            (12 * 60 + 30) * 1000L // 12 minutes 30 seconds
        }
    }

    /**
     * Get maximum number of pauses allowed based on number of periods
     * 2 periods = 1 pause (half-time)
     * 4 periods = 3 pauses (between quarters)
     */
    fun getMaxPauses(): Int {
        return numberOfPeriods - 1
    }

    /**
     * Check if match can be paused
     */
    fun canPause(): Boolean {
        return pauseCount < getMaxPauses()
    }

    /**
     * Check if match is in last period
     */
    fun isLastPeriod(): Boolean {
        return currentPeriod >= numberOfPeriods
    }
}
