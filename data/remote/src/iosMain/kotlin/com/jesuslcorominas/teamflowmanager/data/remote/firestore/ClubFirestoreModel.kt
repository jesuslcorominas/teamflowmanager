package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import kotlinx.serialization.Serializable

@Serializable
data class ClubFirestoreModel(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val invitationCode: String = "",
    val homeGround: String? = null,
)

fun ClubFirestoreModel.toDomain(): Club =
    Club(
        id = id,
        ownerId = ownerId,
        name = name,
        invitationCode = invitationCode,
        homeGround = homeGround,
    )
