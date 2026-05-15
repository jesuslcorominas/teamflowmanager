package com.jesuslcorominas.teamflowmanager.domain.model

data class PlayerSubstitution(
    val id: String = "",
    val matchId: String,
    val playerOutId: String,
    val playerInId: String,
    val substitutionTimeMillis: Long,
    val matchElapsedTimeMillis: Long,
    val operationId: String? = null,
)
