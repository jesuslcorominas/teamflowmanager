package com.jesuslcorominas.teamflowmanager.domain.model

data class PlayerTimer(
    val playerId: String,
    val playerName: String,
    val elapsedTime: Long = 0,
    val isActive: Boolean = false
)
