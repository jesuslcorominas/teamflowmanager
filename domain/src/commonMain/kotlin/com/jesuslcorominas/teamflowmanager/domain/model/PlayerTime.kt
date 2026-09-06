package com.jesuslcorominas.teamflowmanager.domain.model

data class PlayerTime(
    val playerId: String,
    val matchId: String = "",
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
    val status: PlayerTimeStatus = PlayerTimeStatus.ON_BENCH,
    val lastOperationId: String? = null,
)
