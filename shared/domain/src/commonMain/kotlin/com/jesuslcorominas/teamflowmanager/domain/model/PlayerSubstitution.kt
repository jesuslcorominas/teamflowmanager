package com.jesuslcorominas.teamflowmanager.domain.model

data class PlayerSubstitution(
    val id: Long = 0L,
    val matchId: Long,
    val playerOutId: Long,
    val playerInId: Long,
    val substitutionTimeMillis: Long,
    val matchElapsedTimeMillis: Long,
)
