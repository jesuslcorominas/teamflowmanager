package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.domain.model.Club

/**
 * Firestore model for Club document.
 */
data class ClubFirestoreModel(
    @DocumentId
    override val id: String = "",
    override val ownerId: String = "",
    override val name: String = "",
    override val invitationCode: String = "",
    override val homeGround: String? = null,
) : ClubFields {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        ownerId = "",
        name = "",
        invitationCode = "",
        homeGround = null,
    )
}

fun ClubFirestoreModel.toDomain(): Club =
    Club(
        id = id,
        ownerId = ownerId,
        name = name,
        invitationCode = invitationCode,
        homeGround = homeGround,
    )

fun Club.toFirestoreModel(): ClubFirestoreModel =
    ClubFirestoreModel(
        id = id,
        ownerId = ownerId,
        name = name,
        invitationCode = invitationCode,
        homeGround = homeGround,
    )
