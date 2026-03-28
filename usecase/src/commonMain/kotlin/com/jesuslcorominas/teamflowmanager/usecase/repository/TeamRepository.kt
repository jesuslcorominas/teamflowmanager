package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun getTeam(): Flow<Team?>

    suspend fun createTeam(team: Team)

    suspend fun updateTeam(team: Team)

    /**
     * Get team by coach ID.
     * @param coachId The Firebase user ID of the coach
     * @return Flow emitting the team if found, null otherwise
     */
    fun getTeamByCoachId(coachId: String): Flow<Team?>

    /**
     * Get all teams for a club.
     * @param clubId The identifier of the club
     * @return Flow emitting list of teams in the club
     */
    fun getTeamsByClub(clubId: String): Flow<List<Team>>

    /**
     * Get orphan teams for a user (teams without a clubId).
     * @param ownerId The Firebase user ID of the team owner
     * @return List of teams without clubId
     */
    suspend fun getOrphanTeams(ownerId: String): List<Team>

    /**
     * Update the club assignment of a team.
     * @param teamId The identifier of the team
     * @param clubNumericId The club's local numeric ID
     * @param clubId The club's string identifier
     */
    suspend fun updateTeamClubId(
        teamId: String,
        clubNumericId: Long,
        clubId: String,
    )

    /**
     * Get team by its identifier.
     * @param teamId The identifier of the team
     * @return The team if found, null otherwise
     */
    suspend fun getTeamById(teamId: String): Team?

    /**
     * Update the coachId of a team.
     * @param teamId The identifier of the team
     * @param coachId The Firebase user ID of the coach to assign
     */
    suspend fun updateTeamCoachId(
        teamId: String,
        coachId: String,
    )

    /**
     * Generate a shareable invitation link for a team.
     * @param teamId The identifier of the team
     * @param teamName The name of the team for display in the invitation
     * @return The generated invitation link
     */
    suspend fun generateTeamInvitationLink(
        teamId: String,
        teamName: String,
    ): String
}
