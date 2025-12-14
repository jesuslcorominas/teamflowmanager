package com.jesuslcorominas.teamflowmanager.domain.model

data class Club(
    val id: String,
    val ownerId: String,
    val name: String,
    val invitationCode: String,
)
