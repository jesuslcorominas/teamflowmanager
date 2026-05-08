package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import kotlinx.serialization.Serializable

@Serializable
data class ClubFirestoreModel(
    override val id: String = "",
    override val ownerId: String = "",
    override val name: String = "",
    override val invitationCode: String = "",
    override val homeGround: String? = null,
) : ClubFields
