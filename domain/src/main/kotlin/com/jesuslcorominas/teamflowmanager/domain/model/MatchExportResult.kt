package com.jesuslcorominas.teamflowmanager.domain.model

data class MatchExportResult(
    val match: Match,
    val date: Long,
    val opponent: String,
    val location: String,
    val teamGoals: Int,
    val opponentGoals: Int,
)
