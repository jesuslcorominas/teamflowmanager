package com.jesuslcorominas.teamflowmanager.domain.model

data class SkeletonMatch(
    val opponent: String,
    val location: String,
    val dateTime: Long?,
    val numberOfPeriods: Int,
    val squadCallUpIds: List<String>,
    val captainId: String,
    val startingLineupIds: List<String>,
)
