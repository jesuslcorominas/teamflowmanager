package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import kotlinx.serialization.Serializable

@Serializable
data class ClubMemberFirestoreModel(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val clubId: String = "",
    val roles: List<String> = emptyList(),
)

fun ClubMemberFirestoreModel.toDomain(): ClubMember =
    ClubMember(
        id = id.toStableId(),
        userId = userId,
        name = name,
        email = email,
        clubId = clubId.toStableId(),
        roles = roles,
        remoteId = id,
        clubRemoteId = clubId,
    )
