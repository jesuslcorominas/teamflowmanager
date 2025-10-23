package com.jesuslcorominas.teamflowmanager.domain.model

data class Goal(
    val id: Long = 0L,
    val matchId: Long,
    val scorerId: Long?,
    val goalTimeMillis: Long,
    val matchElapsedTimeMillis: Long,
    val isOpponentGoal: Boolean = false,
)
