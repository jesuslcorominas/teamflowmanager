package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * Domain model representing a player
 */
data class Player(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val positions: List<String>
)
