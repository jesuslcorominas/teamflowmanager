package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PlayerFirestoreModel(
    @Transient val id: String = "",
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
        isCaptain = captain,
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
        captain = isCaptain,
        imageUri = imageUri,
        deleted = deleted,
    )
