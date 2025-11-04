package com.jesuslcorominas.teamflowmanager.data.local.entity

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus

data class PlayerTimeEntity(
    val playerId: Long,
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
    val status: String = PlayerTimeStatus.ON_BENCH.name,
)

fun PlayerTimeEntity.toDomain(): PlayerTime =
    PlayerTime(
        playerId = playerId,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
        status = try { PlayerTimeStatus.valueOf(status) } catch (e: Exception) { PlayerTimeStatus.ON_BENCH },
    )

fun PlayerTime.toEntity(): PlayerTimeEntity =
    PlayerTimeEntity(
        playerId = playerId,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
        status = status.name,
    )
