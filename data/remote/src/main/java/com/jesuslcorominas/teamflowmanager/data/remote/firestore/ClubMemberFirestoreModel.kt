package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember

/**
 * Firestore model for ClubMember document.
 * This model is used for serialization/deserialization with Firestore.
 * The `id` field is automatically populated by Firestore with the document ID.
 * The `clubId` field is used by Firestore security rules to validate access.
 */
data class ClubMemberFirestoreModel(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val clubId: String = "",
    val role: String = "",
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        userId = "",
        name = "",
        email = "",
        clubId = "",
        role = "",
    )
}

fun ClubMemberFirestoreModel.toDomain(): ClubMember =
    ClubMember(
        id = id,
        userId = userId,
        name = name,
        email = email,
        clubId = clubId,
        role = role,
    )

fun ClubMember.toFirestoreModel(): ClubMemberFirestoreModel =
    ClubMemberFirestoreModel(
        id = id,
        userId = userId,
        name = name,
        email = email,
        clubId = clubId,
        role = role,
    )
