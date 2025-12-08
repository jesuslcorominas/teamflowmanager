package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position

@Entity(
    tableName = "players",
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("teamId")],
)
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val number: Int,
    val positions: String,
    val teamId: Long = 1,
    val isCaptain: Boolean = false,
    val imageUri: String? = null,
    val deleted: Boolean = false,
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
        imageUri = imageUri,
        deleted = deleted,
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
        imageUri = imageUri,
        deleted = deleted,
    )
