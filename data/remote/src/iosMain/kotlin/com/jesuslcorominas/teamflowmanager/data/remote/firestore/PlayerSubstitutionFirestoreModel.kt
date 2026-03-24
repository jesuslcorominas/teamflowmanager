package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * iOS Firestore model for PlayerSubstitution — uses @Serializable (kotlinx.serialization).
 * The document ID is injected externally from DocumentSnapshot.id.
 */
@Serializable
data class PlayerSubstitutionFirestoreModel(
    @Transient val id: String = "",
    val teamId: String = "",
    val matchId: Long = 0L,
    val matchDocId: String = "",
    val playerOutId: Long = 0L,
    val playerInId: Long = 0L,
    val substitutionTimeMillis: Long = 0L,
    val matchElapsedTimeMillis: Long = 0L,
)

fun PlayerSubstitutionFirestoreModel.toDomain(): PlayerSubstitution =
    PlayerSubstitution(
        id = id.toStableId(),
        matchId = matchId,
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = substitutionTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
    )

fun PlayerSubstitution.toFirestoreModel(): PlayerSubstitutionFirestoreModel =
    PlayerSubstitutionFirestoreModel(
        teamId = "",
        matchId = matchId,
        matchDocId = "",
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = substitutionTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
    )
