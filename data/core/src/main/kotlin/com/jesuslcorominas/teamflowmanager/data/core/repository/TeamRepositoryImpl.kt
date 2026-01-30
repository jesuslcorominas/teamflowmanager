package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.DynamicLinkDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.Flow

internal class TeamRepositoryImpl(
    private val teamDataSource: TeamDataSource,
    private val dynamicLinkDataSource: DynamicLinkDataSource,
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

    override fun getTeamsByClub(clubFirestoreId: String): Flow<List<Team>> =
        teamDataSource.getTeamsByClub(clubFirestoreId)

    override suspend fun getOrphanTeams(ownerId: String): List<Team> {
        return teamDataSource.getOrphanTeams(ownerId)
    }

    override suspend fun updateTeamClubId(teamFirestoreId: String, clubId: Long, clubFirestoreId: String) {
        teamDataSource.updateTeamClubId(teamFirestoreId, clubId, clubFirestoreId)
    }

    override suspend fun getTeamByFirestoreId(teamFirestoreId: String): Team? {
        return teamDataSource.getTeamByFirestoreId(teamFirestoreId)
    }

    override suspend fun updateTeamCoachId(teamFirestoreId: String, coachId: String) {
        teamDataSource.updateTeamCoachId(teamFirestoreId, coachId)
    }

    override suspend fun generateTeamInvitationLink(teamFirestoreId: String, teamName: String): String {
        return dynamicLinkDataSource.generateTeamInvitationLink(teamFirestoreId, teamName)
    }
}
