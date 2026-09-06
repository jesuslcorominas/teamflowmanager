package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PlayerTimeHistoryFirestoreModel(
    @Transient val id: String = "",
    val teamId: String = "",
    val playerId: String = "",
    val matchId: String = "",
    val elapsedTimeMillis: Long = 0L,
    val savedAtMillis: Long = 0L,
)

fun PlayerTimeHistoryFirestoreModel.toDomain(): PlayerTimeHistory =
    PlayerTimeHistory(
        id = id,
        playerId = playerId,
        matchId = matchId,
        elapsedTimeMillis = elapsedTimeMillis,
        savedAtMillis = savedAtMillis,
    )

fun PlayerTimeHistory.toFirestoreModel(): PlayerTimeHistoryFirestoreModel =
    PlayerTimeHistoryFirestoreModel(
        id = id,
        teamId = "",
        playerId = playerId,
        matchId = matchId,
        elapsedTimeMillis = elapsedTimeMillis,
        savedAtMillis = savedAtMillis,
    )
