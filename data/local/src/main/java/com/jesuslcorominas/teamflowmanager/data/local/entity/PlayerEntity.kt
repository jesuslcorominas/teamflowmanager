package com.jesuslcorominas.teamflowmanager.data.local.entity

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position

data class PlayerEntity(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val number: Int,
    val positions: String,
    val teamId: Long = 1,
    val isCaptain: Boolean = false,
)

fun PlayerEntity.toDomain(): Player =
    Player(
        id = id,
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions =
            positions
                .split(",")
                .mapNotNull { Position.fromId(it.trim()) },
        teamId = teamId,
        isCaptain = isCaptain,
    )

fun Player.toEntity(): PlayerEntity =
    PlayerEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions = positions.joinToString(",") { it.id },
        teamId = teamId,
        isCaptain = isCaptain,
    )
