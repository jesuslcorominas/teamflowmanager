package com.jesuslcorominas.teamflowmanager.domain.model

data class PlayerTimeHistory(
    val id: Long = 0L,
    val playerId: Long,
    val matchId: Long,
    val elapsedTimeMillis: Long,
    val savedAtMillis: Long,
)
