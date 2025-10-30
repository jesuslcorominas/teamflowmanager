package com.jesuslcorominas.teamflowmanager.domain.model

data class PlayerExportStats(
    val player: Player,
    val matchesCalledUp: Int,
    val matchesPlayed: Int,
    val totalTimeMinutes: Double,
    val averageTimePerMatch: Double,
    val goalsScored: Int,
)
