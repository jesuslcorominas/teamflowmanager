package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory

/**
 * Firestore model for PlayerTimeHistory document.
 * This model is used for serialization/deserialization with Firestore.
 * PlayerTimeHistory stores historical records of player time for completed matches.
 * The `teamId` field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
data class PlayerTimeHistoryFirestoreModel(
    @DocumentId
    val id: String = "",
    val teamId: String = "",
    val playerId: Long = 0L,
    val matchId: Long = 0L,
    val elapsedTimeMillis: Long = 0L,
    val savedAtMillis: Long = 0L,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        teamId = "",
        playerId = 0L,
        matchId = 0L,
        elapsedTimeMillis = 0L,
        savedAtMillis = 0L,
    )
}

fun PlayerTimeHistoryFirestoreModel.toDomain(): PlayerTimeHistory =
    PlayerTimeHistory(
        id = id.toStableId(),
        playerId = playerId,
        matchId = matchId,
        elapsedTimeMillis = elapsedTimeMillis,
        savedAtMillis = savedAtMillis,
    )

fun PlayerTimeHistory.toFirestoreModel(): PlayerTimeHistoryFirestoreModel =
    PlayerTimeHistoryFirestoreModel(
        id = "", // Will be set when inserting
        teamId = "", // Will be set by the data source
        playerId = playerId,
        matchId = matchId,
        elapsedTimeMillis = elapsedTimeMillis,
        savedAtMillis = savedAtMillis,
    )
