package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * iOS Firestore model for PlayerTime — uses @Serializable (kotlinx.serialization).
 * Note: Android uses @PropertyName("running") for isRunning, so the Firestore field is "running".
 * We mirror that field name directly here.
 * PlayerTime documents use "player_{playerId}" as document ID (not auto-generated).
 * The document ID is injected externally.
 */
@Serializable
data class PlayerTimeFirestoreModel(
    @Transient val id: String = "",
    val teamId: String = "",
    val matchId: Long = 0L,
    val playerId: Long = 0L,
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
        teamId = "",
        matchId = matchId,
        playerId = playerId,
        elapsedTimeMillis = elapsedTimeMillis,
        running = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
        status = status.name,
        lastOperationId = lastOperationId,
    )
