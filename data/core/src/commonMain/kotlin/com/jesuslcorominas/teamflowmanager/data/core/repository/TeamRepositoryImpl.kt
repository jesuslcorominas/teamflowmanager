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

    override fun getTeamByCoachId(coachId: String): Flow<Team?> = teamDataSource.getTeamByCoachId(coachId)

    override fun getTeamsByClub(clubId: String): Flow<List<Team>> = teamDataSource.getTeamsByClub(clubId)

    override suspend fun getOrphanTeams(ownerId: String): List<Team> {
        return teamDataSource.getOrphanTeams(ownerId)
    }

    override suspend fun updateTeamClubId(
        teamId: String,
        clubNumericId: Long,
        clubId: String,
    ) {
        teamDataSource.updateTeamClubId(teamId, clubNumericId, clubId)
    }

    override suspend fun getTeamById(teamId: String): Team? {
        return teamDataSource.getTeamById(teamId)
    }

    override suspend fun updateTeamCoachId(
        teamId: String,
        coachId: String,
    ) {
        teamDataSource.updateTeamCoachId(teamId, coachId)
    }

    override suspend fun updateTeamPendingCoachEmail(
        teamId: String,
        email: String?,
    ) {
        teamDataSource.updateTeamPendingCoachEmail(teamId, email)
    }

    override suspend fun generateTeamInvitationLink(
        teamId: String,
        teamName: String,
    ): String {
        return dynamicLinkDataSource.generateTeamInvitationLink(teamId, teamName)
    }

    override suspend fun clearTeamCoach(teamId: String) {
        teamDataSource.clearTeamCoach(teamId)
    }
}
