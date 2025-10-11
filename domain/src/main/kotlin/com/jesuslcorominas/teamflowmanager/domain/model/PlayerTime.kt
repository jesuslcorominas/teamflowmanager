package com.jesuslcorominas.teamflowmanager.domain.model

data class PlayerTime(
    val playerId: Long,
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
)
