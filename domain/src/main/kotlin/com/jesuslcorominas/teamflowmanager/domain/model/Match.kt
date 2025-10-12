package com.jesuslcorominas.teamflowmanager.domain.model

data class Match(
    val id: Long = 0L,
    val teamId: Long = 1L,
    val opponent: String? = null,
    val location: String? = null,
    val date: Long? = null, // Timestamp in milliseconds
    val startingLineupIds: List<Long> = emptyList(),
    val substituteIds: List<Long> = emptyList(),
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
    val status: MatchStatus = MatchStatus.PROGRAMADO,
)
