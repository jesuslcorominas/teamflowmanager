package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus

@Entity(
    tableName = "player_time",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("playerId")],
)
data class PlayerTimeEntity(
    @PrimaryKey
    val playerId: Long,
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
    val status: String = PlayerTimeStatus.EN_BANQUILLO.name,
)

fun PlayerTimeEntity.toDomain(): PlayerTime =
    PlayerTime(
        playerId = playerId,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
        status = try { PlayerTimeStatus.valueOf(status) } catch (e: Exception) { PlayerTimeStatus.EN_BANQUILLO },
    )

fun PlayerTime.toEntity(): PlayerTimeEntity =
    PlayerTimeEntity(
        playerId = playerId,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
        status = status.name,
    )
