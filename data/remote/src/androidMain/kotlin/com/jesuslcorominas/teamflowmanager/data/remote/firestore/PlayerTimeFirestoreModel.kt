package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus

/**
 * Firestore model for PlayerTime document.
 */
data class PlayerTimeFirestoreModel(
    @DocumentId
    val id: String = "",
    val teamId: String = "",
    val matchId: String = "",
    val playerId: String = "",
    val elapsedTimeMillis: Long = 0L,
    @get:PropertyName("running")
    @set:PropertyName("running")
    var isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
    val status: String = PlayerTimeStatus.ON_BENCH.name,
    val lastOperationId: String? = null,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        teamId = "",
        matchId = "",
        playerId = "",
        elapsedTimeMillis = 0L,
        isRunning = false,
        lastStartTimeMillis = null,
        status = PlayerTimeStatus.ON_BENCH.name,
        lastOperationId = null,
    )
}

fun PlayerTimeFirestoreModel.toDomain(): PlayerTime =
    PlayerTime(
        matchId = matchId,
        playerId = playerId,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
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
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
        status = status.name,
        lastOperationId = lastOperationId,
    )
