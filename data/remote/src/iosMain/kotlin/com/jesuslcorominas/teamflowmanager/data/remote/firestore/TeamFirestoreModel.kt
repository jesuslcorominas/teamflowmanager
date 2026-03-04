package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TeamFirestoreModel(
    @Transient val id: String = "",
    val name: String = "",
    val coachName: String = "",
    val delegateName: String = "",
    val captainId: Long? = null,
    val teamType: Int = TeamType.FOOTBALL_5.players,
    val assignedCoachId: String? = null,
    val clubId: String? = null,
)

fun TeamFirestoreModel.toDomain(): Team =
    Team(
        id = id.toStableId(),
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = TeamType.fromPlayers(teamType),
        coachId = assignedCoachId,
        clubId = clubId?.takeIf { it.isNotEmpty() }?.toStableId(),
        clubFirestoreId = clubId?.takeIf { it.isNotEmpty() },
        firestoreId = id,
    )

fun Team.toFirestoreModel(): TeamFirestoreModel =
    TeamFirestoreModel(
        id = firestoreId.orEmpty(),
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = teamType.players,
        assignedCoachId = coachId,
        clubId = clubFirestoreId,
    )
