package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * Data class containing all timeline data for a finished match.
 */
data class MatchTimeline(
    val events: List<TimelineEvent>,
    val scoreEvolution: List<ScorePoint>,
    val playerActivity: List<PlayerActivityInterval> = emptyList(),
)
