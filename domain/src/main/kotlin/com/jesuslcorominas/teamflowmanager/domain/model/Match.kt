package com.jesuslcorominas.teamflowmanager.domain.model

import kotlin.collections.filter

data class Match(
    val id: Long = 0L,
    val teamId: Long = 1L,
    val teamName: String,
    val opponent: String,
    val location: String,
    val dateTime: Long? = null,
    val periodType: PeriodType,
    val squadCallUpIds: List<Long> = emptyList(),
    val captainId: Long,
    val startingLineupIds: List<Long> = emptyList(),
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val archived: Boolean = false,
    val pauseCount: Int = 0,
    val goals: Int = 0,
    val opponentGoals: Int = 0,
    val timeoutStartTimeMillis: Long = 0L,
    val periods: List<MatchPeriod> = (1..periodType.numberOfPeriods).map {
        MatchPeriod(
            periodNumber = it,
            periodDuration = PeriodType.fromNumberOfPeriods(periodType.numberOfPeriods).duration
        )
    },
) {
    fun canPause(): Boolean {
        return pauseCount < periodType.numberOfPeriods - 1
    }

    fun isLastPeriod(): Boolean {
        return periods.last().let { it.startTimeMillis != 0L && it.endTimeMillis == 0L }
    }

    // TODO check this. We are not considering additional time
    fun getTotalElapsed(currentTime: Long) = periods
        .filter { it.startTimeMillis > 0 }
        .sumOf { period ->
            val end = if (period.endTimeMillis > 0) period.endTimeMillis else currentTime
            (end - period.startTimeMillis).coerceAtMost(period.periodDuration)
        }

    val isInProgress: Boolean
        get() = status == MatchStatus.IN_PROGRESS

    val isStarted: Boolean
        get() = status == MatchStatus.IN_PROGRESS || status == MatchStatus.PAUSED || status == MatchStatus.TIMEOUT
}

data class MatchPeriod(
    val periodNumber: Int,
    val periodDuration: Long = 0L,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
){
    companion object {
        const val PERIOD_DURATION_TWO_HALF = 25 * 60 * 1000L // 25 minutes in milliseconds
        const val PERIOD_DURATION_FOUR_QUARTERS = ((12 * 60) + 30) * 1000L // 12 minutes 30 seconds in milliseconds
    }
}

enum class PeriodType(val numberOfPeriods: Int, val duration: Long) {
    HALF_TIME(2, MatchPeriod.PERIOD_DURATION_TWO_HALF),
    QUARTER_TIME(4, MatchPeriod.PERIOD_DURATION_FOUR_QUARTERS);

    companion object {
        fun fromNumberOfPeriods(numberOfPeriods: Int): PeriodType {
            return PeriodType.entries.find { it.numberOfPeriods == numberOfPeriods } ?: HALF_TIME
        }
    }
}
