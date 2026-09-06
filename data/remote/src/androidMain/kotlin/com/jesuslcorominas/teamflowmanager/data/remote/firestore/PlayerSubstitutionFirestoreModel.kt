package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution

/**
 * Firestore model for PlayerSubstitution document.
 */
data class PlayerSubstitutionFirestoreModel(
    @DocumentId
    val id: String = "",
    val teamId: String = "",
    val matchId: String = "",
    val playerOutId: String = "",
    val playerInId: String = "",
    val substitutionTimeMillis: Long = 0L,
    val matchElapsedTimeMillis: Long = 0L,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        teamId = "",
        matchId = "",
        playerOutId = "",
        playerInId = "",
        substitutionTimeMillis = 0L,
        matchElapsedTimeMillis = 0L,
    )
}

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
