package com.jesuslcorominas.teamflowmanager.domain.model

import java.time.LocalDate

data class Player(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val positions: List<Position>
)
