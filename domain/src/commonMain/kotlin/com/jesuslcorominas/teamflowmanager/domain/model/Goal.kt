package com.jesuslcorominas.teamflowmanager.domain.model

data class Goal(
    val id: String = "",
    val matchId: String,
    val scorerId: String?,
    val goalTimeMillis: Long,
    val matchElapsedTimeMillis: Long,
    val isOpponentGoal: Boolean = false,
    val isOwnGoal: Boolean = false,
)
