package com.jesuslcorominas.teamflowmanager.domain.model

data class Team(
    val id: String,
    val name: String,
    val coachName: String,
    val delegateName: String,
    val captainId: String? = null,
    val teamType: TeamType,
    val coachId: String? = null, // ID of the assigned coach (null if no coach assigned)
    val pendingCoachEmail: String? = null, // Email pending assignment (not yet a club member)
    val clubId: String? = null,
)
