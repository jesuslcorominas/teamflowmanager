package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType

/**
 * Firestore model for Team document.
 */
data class TeamFirestoreModel(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val coachName: String = "",
    val delegateName: String = "",
    val captainId: String? = null,
    val teamType: Int = TeamType.FOOTBALL_5.players,
    val assignedCoachId: String? = null,
    val pendingCoachEmail: String? = null,
    val clubId: String? = null,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        name = "",
        coachName = "",
        delegateName = "",
        captainId = null,
        teamType = TeamType.FOOTBALL_5.players,
        assignedCoachId = null,
        pendingCoachEmail = null,
        clubId = null,
    )
}

fun TeamFirestoreModel.toDomain(): Team =
    Team(
        id = id,
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = TeamType.fromPlayers(teamType),
        coachId = assignedCoachId,
        pendingCoachEmail = pendingCoachEmail?.takeIf { it.isNotEmpty() },
        clubId = clubId?.takeIf { it.isNotEmpty() },
    )

fun Team.toFirestoreModel(): TeamFirestoreModel =
    TeamFirestoreModel(
        id = id,
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = teamType.players,
        assignedCoachId = coachId,
        clubId = clubId,
    )
