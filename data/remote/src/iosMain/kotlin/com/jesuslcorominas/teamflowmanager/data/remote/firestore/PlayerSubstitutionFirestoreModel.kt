package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PlayerSubstitutionFirestoreModel(
    @Transient val id: String = "",
    val teamId: String = "",
    val matchId: String = "",
    val playerOutId: String = "",
    val playerInId: String = "",
    val substitutionTimeMillis: Long = 0L,
    val matchElapsedTimeMillis: Long = 0L,
)

fun PlayerSubstitutionFirestoreModel.toDomain(): PlayerSubstitution =
    PlayerSubstitution(
        id = id,
        matchId = matchId,
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = substitutionTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
    )

fun PlayerSubstitution.toFirestoreModel(): PlayerSubstitutionFirestoreModel =
    PlayerSubstitutionFirestoreModel(
        id = id,
        teamId = "",
        matchId = matchId,
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = substitutionTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
    )
