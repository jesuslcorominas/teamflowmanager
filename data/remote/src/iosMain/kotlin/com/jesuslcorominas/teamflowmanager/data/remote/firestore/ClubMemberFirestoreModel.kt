package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ClubMemberFirestoreModel(
    // Not written to the document, like Android's @DocumentId: reads copy in doc.id.
    @Transient val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val clubId: String = "",
    val roles: List<String> = emptyList(),
)

fun ClubMemberFirestoreModel.toDomain(): ClubMember =
    ClubMember(
        id = id,
        userId = userId,
        name = name,
        email = email,
        clubId = clubId,
        roles = roles,
    )
