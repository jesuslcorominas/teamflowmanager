package com.jesuslcorominas.teamflowmanager.domain.model

data class Match(
    val id: Long = 0L,
    val teamId: Long = 1L,
    val opponent: String? = null,
    val location: String? = null,
    val date: Long? = null, // Date in milliseconds
    val time: Long? = null, // Time in milliseconds (hours and minutes of day)
    val numberOfPeriods: Int = 2, // Number of periods: 2 for halves, 4 for quarters
    val squadCallUpIds: List<Long> = emptyList(), // Players selected for match squad (convocatoria)
    val captainId: Long? = null, // Captain for this match
    val startingLineupIds: List<Long> = emptyList(),
    val substituteIds: List<Long> = emptyList(),
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
    val status: MatchStatus = MatchStatus.SCHEDULED,
)
