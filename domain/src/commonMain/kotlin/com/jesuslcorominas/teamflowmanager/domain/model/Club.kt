package com.jesuslcorominas.teamflowmanager.domain.model

data class Club(
    val id: Long,
    val ownerId: String,
    val name: String,
    val invitationCode: String,
    val firestoreId: String? = null,
    val homeGround: String? = null,
)
