package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
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
        id = id.toStableId(),
        ownerId = ownerId,
        name = name,
        invitationCode = invitationCode,
        firestoreId = id,
    )

fun Club.toFirestoreModel(): ClubFirestoreModel =
    ClubFirestoreModel(
        id = firestoreId ?: "", // Will be set when inserting/updating
        ownerId = ownerId,
        name = name,
        invitationCode = invitationCode,
    )
