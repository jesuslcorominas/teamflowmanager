package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember

/**
 * Firestore model for ClubMember document.
 */
data class ClubMemberFirestoreModel(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val clubId: String = "",
    val roles: List<String> = emptyList(),
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        userId = "",
        name = "",
        email = "",
        clubId = "",
        roles = emptyList(),
    )
}

fun ClubMemberFirestoreModel.toDomain(): ClubMember =
    ClubMember(
        id = id,
        userId = userId,
        name = name,
        email = email,
        clubId = clubId,
        roles = roles,
    )

fun ClubMember.toFirestoreModel(): ClubMemberFirestoreModel =
    ClubMemberFirestoreModel(
        id = id,
        userId = userId,
        name = name,
        email = email,
        clubId = clubId,
        roles = roles,
    )
