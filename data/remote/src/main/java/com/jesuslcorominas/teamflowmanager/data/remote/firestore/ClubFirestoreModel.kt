package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.domain.model.Club

/**
 * Firestore model for Club document.
 * This model is used for serialization/deserialization with Firestore.
 * The `id` field is automatically populated by Firestore with the document ID.
 * The `ownerId` field is required by Firestore security rules to identify the owner.
 */
data class ClubFirestoreModel(
    @DocumentId
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val invitationCode: String = "",
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        ownerId = "",
        name = "",
        invitationCode = "",
    )
}

fun ClubFirestoreModel.toDomain(): Club =
    Club(
        id = id,
        ownerId = ownerId,
        name = name,
        invitationCode = invitationCode,
    )

fun Club.toFirestoreModel(): ClubFirestoreModel =
    ClubFirestoreModel(
        id = id,
        ownerId = ownerId,
        name = name,
        invitationCode = invitationCode,
    )
