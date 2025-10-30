package com.jesuslcorominas.teamflowmanager.domain.model

data class PlayerTimeStats(
    val player: Player,
    val totalTimeMinutes: Double,
    val matchesPlayed: Int,
)
