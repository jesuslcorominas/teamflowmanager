package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import java.time.LocalDate

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String, // Stored as ISO-8601 string (YYYY-MM-DD)
    val positions: String
)

fun PlayerEntity.toDomain(): Player {
    return Player(
        id = id,
        firstName = firstName,
        lastName = lastName,
        dateOfBirth = LocalDate.parse(dateOfBirth),
        positions = positions.split(",")
            .mapNotNull { Position.fromId(it.trim()) }
    )
}

fun Player.toEntity(): PlayerEntity {
    return PlayerEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        dateOfBirth = dateOfBirth.toString(),
        positions = positions.joinToString(",") { it.id }
    )
}
