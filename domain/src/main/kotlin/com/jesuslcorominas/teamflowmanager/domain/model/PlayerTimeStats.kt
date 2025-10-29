package com.jesuslcorominas.teamflowmanager.domain.model

data class PlayerTimeStats(
    val player: Player,
    val totalTimeMillis: Long,
    val matchesPlayed: Int,
)
