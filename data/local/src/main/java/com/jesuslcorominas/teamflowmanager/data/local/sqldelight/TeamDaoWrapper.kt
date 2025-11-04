package com.jesuslcorominas.teamflowmanager.data.local.sqldelight

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.entity.TeamEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class TeamDaoWrapper(
    private val database: TeamFlowManagerDatabase
) {
    fun getTeam(): Flow<TeamEntity?> =
        database.teamQueries
            .getTeam()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { team ->
                team?.let {
                    TeamEntity(
                        id = it.id,
                        name = it.name,
                        coachName = it.coachName,
                        delegateName = it.delegateName,
                        captainId = it.captainId
                    )
                }
            }

    suspend fun insertTeam(team: TeamEntity) {
        database.teamQueries.insertTeam(
            name = team.name,
            coachName = team.coachName,
            delegateName = team.delegateName,
            captainId = team.captainId
        )
    }

    suspend fun updateTeam(team: TeamEntity) {
        database.teamQueries.updateTeam(
            name = team.name,
            coachName = team.coachName,
            delegateName = team.delegateName,
            captainId = team.captainId,
            id = team.id
        )
    }
}
