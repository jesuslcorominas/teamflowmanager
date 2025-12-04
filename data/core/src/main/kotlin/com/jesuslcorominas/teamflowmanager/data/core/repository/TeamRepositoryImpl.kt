package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.Flow

internal class TeamRepositoryImpl(
    private val localDataSource: TeamLocalDataSource,
) : TeamRepository {
    override fun getTeam(): Flow<Team?> = localDataSource.getTeam()

    override suspend fun createTeam(team: Team) {
        localDataSource.insertTeam(team)
    }

    override suspend fun updateTeam(team: Team) {
        localDataSource.updateTeam(team)
    }

    override fun getTeamByCoachId(coachId: String): Flow<Team?> =
        localDataSource.getTeamByCoachId(coachId)
}
