package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PlayerTimeFirestoreModel(
    @Transient val id: String = "",
    val teamId: String = "",
    val matchId: String = "",
    val playerId: String = "",
    val elapsedTimeMillis: Long = 0L,
    val running: Boolean = false,
    val lastStartTimeMillis: Long? = null,
    val status: String = PlayerTimeStatus.ON_BENCH.name,
    val lastOperationId: String? = null,
)

fun PlayerTimeFirestoreModel.toDomain(): PlayerTime =
    PlayerTime(
        matchId = matchId,
        playerId = playerId,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = running,
        lastStartTimeMillis = lastStartTimeMillis,
        status =
            try {
                PlayerTimeStatus.valueOf(status)
            } catch (_: Exception) {
                PlayerTimeStatus.ON_BENCH
            },
        lastOperationId = lastOperationId,
    )

fun PlayerTime.toFirestoreModel(): PlayerTimeFirestoreModel =
    PlayerTimeFirestoreModel(
        id = playerId,
        teamId = "",
        matchId = matchId,
        playerId = playerId,
        elapsedTimeMillis = elapsedTimeMillis,
        running = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
        status = status.name,
        lastOperationId = lastOperationId,
    )
