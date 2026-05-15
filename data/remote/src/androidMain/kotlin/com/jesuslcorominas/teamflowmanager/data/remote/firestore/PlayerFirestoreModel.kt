package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position

/**
 * Firestore model for Player document.
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
        id = id,
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions =
            positions
                .split(",")
                .filter { it.isNotBlank() }
                .mapNotNull { Position.fromId(it.trim()) },
        teamId = teamId,
        isCaptain = isCaptain,
        imageUri = imageUri,
        deleted = deleted,
    )

fun Player.toFirestoreModel(): PlayerFirestoreModel =
    PlayerFirestoreModel(
        id = id,
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions = positions.joinToString(",") { it.id },
        teamId = teamId,
        isCaptain = isCaptain,
        imageUri = imageUri,
        deleted = deleted,
    )
