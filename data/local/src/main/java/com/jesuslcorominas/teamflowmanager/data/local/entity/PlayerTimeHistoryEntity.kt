package com.jesuslcorominas.teamflowmanager.data.local.entity

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory

data class PlayerTimeHistoryEntity(
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
