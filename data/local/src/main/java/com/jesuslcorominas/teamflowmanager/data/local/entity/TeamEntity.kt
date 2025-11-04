package com.jesuslcorominas.teamflowmanager.data.local.entity

import com.jesuslcorominas.teamflowmanager.domain.model.Team

data class TeamEntity(
    val id: Long = 0,
    val name: String,
    val coachName: String,
    val delegateName: String,
    val captainId: Long? = null,
)

fun TeamEntity.toDomain(): Team =
    Team(
        id = id,
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
    )

fun Team.toEntity(): TeamEntity =
    TeamEntity(
        id = id,
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
    )
