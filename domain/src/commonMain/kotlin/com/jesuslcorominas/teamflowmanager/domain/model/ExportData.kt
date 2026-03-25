package com.jesuslcorominas.teamflowmanager.domain.model

data class ExportData(
    val playerStats: List<PlayerExportStats>,
    val topScorers: List<PlayerGoalStats>,
    val matchResults: List<MatchExportResult>,
)
