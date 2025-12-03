package com.jesuslcorominas.teamflowmanager.domain.model

data class MatchReportData(
    val match: Match,
    val playerReports: List<PlayerMatchReport>,
    val timelineEvents: List<TimelineEvent> = emptyList(),
    val scoreEvolution: List<ScorePoint> = emptyList(),
)

data class PlayerMatchReport(
    val player: Player,
    val number: Int,
    val isGoalkeeper: Boolean,
    val isCaptain: Boolean,
    val isStarter: Boolean,
    val totalPlayTimeMillis: Long,
    val goals: List<GoalReport>,
    val substitutions: List<SubstitutionReport>,
)

data class GoalReport(
    val matchElapsedTimeMillis: Long,
    val isOwnGoal: Boolean = false,
)

data class SubstitutionReport(
    val type: SubstitutionType,
    val matchElapsedTimeMillis: Long,
)

enum class SubstitutionType {
    IN,
    OUT
}
