package com.jesuslcorominas.teamflowmanager.domain.model

data class Player(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val number: Int,
    val positions: List<Position>,
    val teamId: Long = 1,
    val isCaptain: Boolean = false,
)
