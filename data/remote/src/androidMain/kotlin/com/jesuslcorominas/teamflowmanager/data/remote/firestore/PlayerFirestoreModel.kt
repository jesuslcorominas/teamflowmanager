package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position

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
    @get:PropertyName("captain")
    @set:PropertyName("captain")
    var isCaptain: Boolean = false,
    val imageUri: String? = null,
    val deleted: Boolean = false,
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
        deleted = false,
    )
}

fun PlayerFirestoreModel.toDomain(): Player =
    Player(
        id = id.toStableId(),
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions =
            positions
                .split(",")
                .filter { it.isNotBlank() }
                .mapNotNull { Position.fromId(it.trim()) },
        teamId = teamId.toStableId(),
        isCaptain = isCaptain,
        imageUri = imageUri,
        deleted = deleted,
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
        deleted = deleted,
    )
