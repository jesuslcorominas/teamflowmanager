package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType

@Entity(tableName = "team")
data class TeamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val coachName: String,
    val delegateName: String,
    val captainId: Long? = null,
    val teamType: Int = 11,
)

fun TeamEntity.toDomain(): Team =
    Team(
        id = id,
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = TeamType.fromPlayers(teamType),
    )

fun Team.toEntity(): TeamEntity =
    TeamEntity(
        id = id,
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = teamType.players,
    )
