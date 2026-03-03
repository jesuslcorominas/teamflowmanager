package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import kotlinx.serialization.Serializable

/**
 * iOS Firestore model for Player — uses @Serializable (kotlinx.serialization).
 * Note: the field name is "captain" in Firestore (matches Android's @PropertyName("captain")).
 * The document ID is injected externally from DocumentSnapshot.id.
 */
@Serializable
data class PlayerFirestoreModel(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val number: Int = 0,
    val positions: String = "",
    val teamId: String = "",
    val captain: Boolean = false,
    val imageUri: String? = null,
    val deleted: Boolean = false,
)

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
        isCaptain = captain,
        imageUri = imageUri,
        deleted = deleted,
    )

fun Player.toFirestoreModel(): PlayerFirestoreModel =
    PlayerFirestoreModel(
        id = "",
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions = positions.joinToString(",") { it.id },
        teamId = "",
        captain = isCaptain,
        imageUri = imageUri,
        deleted = deleted,
    )
