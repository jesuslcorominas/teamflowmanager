package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.Player

/**
 * Room entity representing a player
 */
@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val positions: String // Stored as comma-separated string
)

/**
 * Extension function to map PlayerEntity to Player domain model
 */
fun PlayerEntity.toDomain(): Player {
    return Player(
        id = id,
        firstName = firstName,
        lastName = lastName,
        positions = positions.split(",").filter { it.isNotBlank() }
    )
}
