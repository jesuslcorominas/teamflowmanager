package com.jesuslcorominas.teamflowmanager.data.local.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import kotlin.math.abs

/**
 * Firestore model for Player document.
 * This model is used for serialization/deserialization with Firestore.
 * The `id` field is automatically populated by Firestore with the document ID.
 * The `teamId` field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
data class PlayerFirestoreModel(
    @DocumentId
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val number: Int = 0,
    val positions: String = "",
    val teamId: String = "",
    val isCaptain: Boolean = false,
    val imageUri: String? = null,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        firstName = "",
        lastName = "",
        number = 0,
        positions = "",
        teamId = "",
        isCaptain = false,
        imageUri = null,
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

fun PlayerFirestoreModel.toDomain(): Player =
    Player(
        id = id.toStableId(),
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions = positions
            .split(",")
            .filter { it.isNotBlank() }
            .mapNotNull { Position.fromId(it.trim()) },
        teamId = teamId.toStableId(),
        isCaptain = isCaptain,
        imageUri = imageUri,
    )

fun Player.toFirestoreModel(): PlayerFirestoreModel =
    PlayerFirestoreModel(
        id = "", // Will be set when inserting/updating
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions = positions.joinToString(",") { it.id },
        teamId = "", // Will be set by the data source
        isCaptain = isCaptain,
        imageUri = imageUri,
    )
