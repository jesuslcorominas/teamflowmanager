package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory

@Entity(
    tableName = "player_time_history",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("playerId"), Index("matchId")],
)
data class PlayerTimeHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val playerId: Long,
    val matchId: Long,
    val elapsedTimeMillis: Long,
    val savedAtMillis: Long,
)

fun PlayerTimeHistoryEntity.toDomain(): PlayerTimeHistory =
    PlayerTimeHistory(
        id = id,
        playerId = playerId,
        matchId = matchId,
        elapsedTimeMillis = elapsedTimeMillis,
        savedAtMillis = savedAtMillis,
    )

fun PlayerTimeHistory.toEntity(): PlayerTimeHistoryEntity =
    PlayerTimeHistoryEntity(
        id = id,
        playerId = playerId,
        matchId = matchId,
        elapsedTimeMillis = elapsedTimeMillis,
        savedAtMillis = savedAtMillis,
    )
