package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.Flow

internal class TeamRepositoryImpl(
    private val teamDataSource: TeamDataSource
) : TeamRepository {
    override fun getTeam(): Flow<Team?> = teamDataSource.getTeam()

    override suspend fun createTeam(team: Team) {
        teamDataSource.insertTeam(team)
    }

    override suspend fun updateTeam(team: Team) {
        teamDataSource.updateTeam(team)
    }

    override fun getTeamByCoachId(coachId: String): Flow<Team?> =
        teamDataSource.getTeamByCoachId(coachId)

    override fun getOrphanTeams(): Flow<List<Team>> =
        teamDataSource.getOrphanTeams()
}
