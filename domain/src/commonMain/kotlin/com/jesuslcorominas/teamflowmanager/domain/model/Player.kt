package com.jesuslcorominas.teamflowmanager.domain.model

data class Player(
    val id: String,
    val firstName: String,
    val lastName: String,
    val number: Int,
    val positions: List<Position>,
    val teamId: String,
    val isCaptain: Boolean,
    val imageUri: String? = null,
    val deleted: Boolean = false,
)
