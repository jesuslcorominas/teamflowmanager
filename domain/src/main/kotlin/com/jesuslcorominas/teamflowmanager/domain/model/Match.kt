package com.jesuslcorominas.teamflowmanager.domain.model

import java.util.Date

data class Match(
    val id: String,
    val teamId: String,
    val opponent: String,
    val startTime: Date?,
    val firstHalfDuration: Long = 0,
    val secondHalfDuration: Long = 0,
    val status: MatchStatus = MatchStatus.NOT_STARTED,
    val activePlayerTimers: List<PlayerTimer> = emptyList()
)

enum class MatchStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PAUSED,
    FINISHED
}
