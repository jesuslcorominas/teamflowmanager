package com.jesuslcorominas.teamflowmanager.domain.model

data class ClubMember(
    val id: String,
    val userId: String,
    val name: String,
    val email: String,
    val clubId: String,
    val role: String,
)
