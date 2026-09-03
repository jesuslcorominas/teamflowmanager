package com.jesuslcorominas.teamflowmanager.domain.model

data class PlayerTimeHistory(
    val id: String = "",
    val playerId: String,
    val matchId: String,
    val elapsedTimeMillis: Long,
    val savedAtMillis: Long,
)
