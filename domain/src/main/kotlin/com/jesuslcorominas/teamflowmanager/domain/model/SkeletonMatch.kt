package com.jesuslcorominas.teamflowmanager.domain.model

data class SkeletonMatch(
    val opponent: String,
    val location: String,
    val dateTime: Long?,
    val numberOfPeriods: Int,
    val squadCallUpIds: List<Long>,
    val captainId: Long?,
    val startingLineupIds: List<Long>,
)
