package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution

/**
 * Firestore model for PlayerSubstitution document.
 * This model is used for serialization/deserialization with Firestore.
 * The `id` field is automatically populated by Firestore with the document ID.
 * The `teamId` field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
data class PlayerSubstitutionFirestoreModel(
    @DocumentId
    val id: String = "",
    val teamId: String = "",
    val matchId: Long = 0L,
    val playerOutId: Long = 0L,
    val playerInId: Long = 0L,
    val substitutionTimeMillis: Long = 0L,
    val matchElapsedTimeMillis: Long = 0L,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        teamId = "",
        matchId = 0L,
        playerOutId = 0L,
        playerInId = 0L,
        substitutionTimeMillis = 0L,
        matchElapsedTimeMillis = 0L,
    )
}

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
        id = "", // Will be set when inserting
        teamId = "", // Will be set by the data source
        matchId = matchId,
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = substitutionTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
    )
