package com.jesuslcorominas.teamflowmanager.data.local.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.entity.toDomain
import com.jesuslcorominas.teamflowmanager.data.local.entity.toEntity
import com.jesuslcorominas.teamflowmanager.data.local.sqldelight.TeamDaoWrapper
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class TeamLocalDataSourceImpl(
    private val teamDao: TeamDaoWrapper,
) : TeamLocalDataSource {
    override fun getTeam(): Flow<Team?> = teamDao.getTeam().map { it?.toDomain() }

    override suspend fun insertTeam(team: Team) {
        teamDao.insertTeam(team.toEntity())
    }

    override suspend fun updateTeam(team: Team) {
        teamDao.updateTeam(team.toEntity())
    }
}
