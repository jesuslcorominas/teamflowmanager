package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution

@Entity(
    tableName = "player_substitution",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerOutId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerInId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("matchId"), Index("playerOutId"), Index("playerInId")],
)
data class PlayerSubstitutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val matchId: Long,
    val playerOutId: Long,
    val playerInId: Long,
    val substitutionTimeMillis: Long,
    val matchElapsedTimeMillis: Long,
)

fun PlayerSubstitutionEntity.toDomain(): PlayerSubstitution =
    PlayerSubstitution(
        id = id,
        matchId = matchId,
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = substitutionTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
    )

fun PlayerSubstitution.toEntity(): PlayerSubstitutionEntity =
    PlayerSubstitutionEntity(
        id = id,
        matchId = matchId,
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = substitutionTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
    )
