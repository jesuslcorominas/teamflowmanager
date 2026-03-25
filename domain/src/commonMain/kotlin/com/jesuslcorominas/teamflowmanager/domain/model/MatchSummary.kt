package com.jesuslcorominas.teamflowmanager.domain.model

data class MatchSummary(
    val match: Match,
    val playerTimes: List<PlayerTimeSummary>,
    val substitutions: List<SubstitutionSummary>,
)

data class PlayerTimeSummary(
    val player: Player,
    val elapsedTimeMillis: Long,
    val substitutionCount: Int,
)

data class SubstitutionSummary(
    val playerOut: Player,
    val playerIn: Player,
    val matchElapsedTimeMillis: Long,
)
