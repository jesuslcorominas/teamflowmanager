package com.jesuslcorominas.teamflowmanager.domain.model

data class ClubMember(
    val id: Long,
    val userId: String,
    val name: String,
    val email: String,
    val clubId: Long,
    val role: String,
    val firestoreId: String? = null,
)
