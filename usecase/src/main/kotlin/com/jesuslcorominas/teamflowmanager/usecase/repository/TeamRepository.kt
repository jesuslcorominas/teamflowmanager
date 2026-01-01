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
     * @param clubFirestoreId The Firestore document ID of the club
     * @return Flow emitting list of teams in the club
     */
    fun getTeamsByClub(clubFirestoreId: String): Flow<List<Team>>

    /**
     * Get orphan teams for a user (teams without a clubId).
     * @param ownerId The Firebase user ID of the team owner
     * @return List of teams without clubId
     */
    suspend fun getOrphanTeams(ownerId: String): List<Team>

    /**
     * Update the clubId of a team.
     * @param teamCoachId The coachId (document ID) of the team
     * @param clubId The club's numeric ID
     * @param clubFirestoreId The club's Firestore document ID
     */
    suspend fun updateTeamClubId(teamCoachId: String, clubId: Long, clubFirestoreId: String)

    /**
     * Get team by Firestore document ID.
     * @param teamFirestoreId The Firestore document ID of the team
     * @return The team if found, null otherwise
     */
    suspend fun getTeamByFirestoreId(teamFirestoreId: String): Team?

    /**
     * Update the coachId of a team.
     * @param teamFirestoreId The Firestore document ID of the team
     * @param coachId The Firebase user ID of the coach to assign
     */
    suspend fun updateTeamCoachId(teamFirestoreId: String, coachId: String)
}
