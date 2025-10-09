package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.Player

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val positions: String
)

fun PlayerEntity.toDomain(): Player {
    return Player(
        id = id,
        firstName = firstName,
        lastName = lastName,
        positions = positions.split(",").filter { it.isNotBlank() }
    )
}
