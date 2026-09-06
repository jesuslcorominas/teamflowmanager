package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory

/**
 * Firestore model for PlayerTimeHistory document.
 */
data class PlayerTimeHistoryFirestoreModel(
    @DocumentId
    val id: String = "",
    val teamId: String = "",
    val playerId: String = "",
    val matchId: String = "",
    val elapsedTimeMillis: Long = 0L,
    val savedAtMillis: Long = 0L,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        teamId = "",
        playerId = "",
        matchId = "",
        elapsedTimeMillis = 0L,
        savedAtMillis = 0L,
    )
}

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
