package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * Represents an event that occurred during a match for timeline display.
 */
sealed class TimelineEvent {
    abstract val matchElapsedTimeMillis: Long

    /**
     * Event representing the starting lineup at the beginning of a match.
     */
    data class StartingLineup(
        override val matchElapsedTimeMillis: Long = 0L,
        val players: List<Player>,
    ) : TimelineEvent()

    /**
     * Event representing a goal scored during the match.
     */
    data class GoalScored(
        override val matchElapsedTimeMillis: Long,
        val scorer: Player?,
        val isOpponentGoal: Boolean,
        val teamScore: Int,
        val opponentScore: Int,
    ) : TimelineEvent()

    /**
     * Event representing a player substitution during the match.
     */
    data class Substitution(
        override val matchElapsedTimeMillis: Long,
        val playerIn: Player,
        val playerOut: Player,
    ) : TimelineEvent()

    /**
     * Event representing a timeout during the match.
     */
    data class Timeout(
        override val matchElapsedTimeMillis: Long,
    ) : TimelineEvent()

    /**
     * Event representing a period break (half-time, quarter break).
     */
    data class PeriodBreak(
        override val matchElapsedTimeMillis: Long,
        val periodNumber: Int,
        val periodType: PeriodType,
    ) : TimelineEvent()
}

/**
 * Represents a score point for the score evolution chart.
 */
data class ScorePoint(
    val timeMillis: Long,
    val teamScore: Int,
    val opponentScore: Int,
    val isOpponentGoal: Boolean,
)

/**
 * Represents the time intervals when a player was active on the field.
 */
data class PlayerActivityInterval(
    val player: Player,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
)
