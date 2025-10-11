package com.jesuslcorominas.teamflowmanager.domain.model

data class Match(
    val id: Long = 1L,
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
)
