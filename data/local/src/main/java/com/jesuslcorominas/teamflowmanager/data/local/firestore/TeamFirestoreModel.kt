package com.jesuslcorominas.teamflowmanager.data.local.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import kotlin.math.abs

/**
 * Firestore model for Team document.
 * This model is used for serialization/deserialization with Firestore.
 */
data class TeamFirestoreModel(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val coachName: String = "",
    val delegateName: String = "",
    val captainId: Long? = null,
    val teamType: Int = TeamType.FOOTBALL_5.players,
    val coachId: String = "",
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        name = "",
        coachName = "",
        delegateName = "",
        captainId = null,
        teamType = TeamType.FOOTBALL_5.players,
        coachId = "",
    )
}

/**
 * Generates a deterministic Long ID from a String document ID.
 * Uses a simplified hash function that is more predictable than hashCode().
 * The ID is based on the ASCII values of the characters to ensure consistency.
 */
private fun String.toStableId(): Long {
    if (isEmpty()) return 0L
    var result = 0L
    var multiplier = 1L
    for (char in this) {
        result += char.code * multiplier
        multiplier *= 31
    }
    return abs(result)
}

fun TeamFirestoreModel.toDomain(): Team =
    Team(
        id = coachId.toStableId(), // Generate a consistent Long id from coachId
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = TeamType.fromPlayers(teamType),
        coachId = coachId,
    )

fun Team.toFirestoreModel(): TeamFirestoreModel =
    TeamFirestoreModel(
        id = coachId ?: "", // Use coachId as document id
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = teamType.players,
        coachId = coachId ?: "",
    )
