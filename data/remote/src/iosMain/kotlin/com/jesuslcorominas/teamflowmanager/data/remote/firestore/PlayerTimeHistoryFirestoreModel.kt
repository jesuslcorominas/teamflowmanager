package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * iOS Firestore model for PlayerTimeHistory — uses @Serializable (kotlinx.serialization).
 * The document ID is injected externally from DocumentSnapshot.id.
 */
@Serializable
data class PlayerTimeHistoryFirestoreModel(
    @Transient val id: String = "",
    val teamId: String = "",
    val playerId: Long = 0L,
    val matchId: Long = 0L,
    val matchDocId: String = "",
    val elapsedTimeMillis: Long = 0L,
    val savedAtMillis: Long = 0L,
)

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
        teamId = "",
        playerId = playerId,
        matchId = matchId,
        matchDocId = "",
        elapsedTimeMillis = elapsedTimeMillis,
        savedAtMillis = savedAtMillis,
    )
