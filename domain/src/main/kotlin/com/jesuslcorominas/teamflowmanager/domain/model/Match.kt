package com.jesuslcorominas.teamflowmanager.domain.model

data class Match(
    val id: Long = 0L,
    val teamId: Long = 1L,
    val teamName: String,
    val opponent: String,
    val location: String,
    val dateTime: Long? = null,
    val numberOfPeriods: Int,
    val squadCallUpIds: List<Long> = emptyList(),
    val captainId: Long? = null,
    val startingLineupIds: List<Long> = emptyList(),
    val elapsedTimeMillis: Long = 0L,
    val lastStartTimeMillis: Long? = null,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val archived: Boolean = false,
    val currentPeriod: Int = 1,
    val pauseCount: Int = 0,
    val goals: Int = 0,
    val opponentGoals: Int = 0,
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

    val isInProgress: Boolean
        get() = status == MatchStatus.IN_PROGRESS

    val isStarted: Boolean
        get() = status == MatchStatus.IN_PROGRESS || status == MatchStatus.PAUSED
}
