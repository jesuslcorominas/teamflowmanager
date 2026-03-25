package com.jesuslcorominas.teamflowmanager.domain.model

data class PlayerGoalStats(
    val player: Player,
    val totalGoals: Int,
    val matchesWithGoals: Int,
)
