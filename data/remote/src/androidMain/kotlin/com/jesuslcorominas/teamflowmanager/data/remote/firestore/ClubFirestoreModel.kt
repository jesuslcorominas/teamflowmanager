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

fun Club.toFirestoreModel(): ClubFirestoreModel =
    ClubFirestoreModel(
        id = remoteId ?: "",
        ownerId = ownerId,
        name = name,
        invitationCode = invitationCode,
        homeGround = homeGround,
    )
