package com.jesuslcorominas.teamflowmanager.domain.model

data class Player(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val number: Int,
    val positions: List<Position>,
)
