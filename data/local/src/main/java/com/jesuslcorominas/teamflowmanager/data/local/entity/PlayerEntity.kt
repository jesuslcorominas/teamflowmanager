package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val number: Int,
    val positions: String,
)

fun PlayerEntity.toDomain(): Player =
    Player(
        id = id,
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions = positions.split(",")
            .mapNotNull { Position.fromId(it.trim()) },
    )

fun Player.toEntity(): PlayerEntity =
    PlayerEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions = positions.joinToString(",") { it.id },
    )
