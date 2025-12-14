package com.jesuslcorominas.teamflowmanager.domain.model

data class Team(
    val id: Long,
    val name: String,
    val coachName: String,
    val delegateName: String,
    val captainId: Long? = null,
    val teamType: TeamType,
    val coachId: String? = null,
    val clubId: Long? = null,
    val clubFirestoreId: String? = null,
)
