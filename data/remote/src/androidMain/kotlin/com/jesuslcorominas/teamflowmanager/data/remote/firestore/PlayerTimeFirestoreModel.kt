package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus

/**
 * Firestore model for PlayerTime document.
 * This model is used for serialization/deserialization with Firestore.
 * PlayerTime represents the current playing time state for a player during an active match.
 * The `teamId` field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
data class PlayerTimeFirestoreModel(
    @DocumentId
    val id: String = "",
    val teamId: String = "",
    val playerId: Long = 0L,
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
        playerId = 0L,
        elapsedTimeMillis = 0L,
        isRunning = false,
        lastStartTimeMillis = null,
        status = PlayerTimeStatus.ON_BENCH.name,
        lastOperationId = null,
    )
}

fun PlayerTimeFirestoreModel.toDomain(): PlayerTime =
    PlayerTime(
        playerId = playerId,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
        status = try {
            PlayerTimeStatus.valueOf(status)
        } catch (_: Exception) {
            PlayerTimeStatus.ON_BENCH
        },
        lastOperationId = lastOperationId,
    )

fun PlayerTime.toFirestoreModel(): PlayerTimeFirestoreModel =
    PlayerTimeFirestoreModel(
        id = "", // Will be set by the data source using playerId
        teamId = "", // Will be set by the data source
        playerId = playerId,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
        status = status.name,
        lastOperationId = lastOperationId,
    )
