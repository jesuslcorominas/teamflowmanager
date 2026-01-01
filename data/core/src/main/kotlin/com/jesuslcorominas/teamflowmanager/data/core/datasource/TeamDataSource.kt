package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface TeamDataSource {
    fun getTeam(): Flow<Team?>

    suspend fun insertTeam(team: Team)

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
     * Check if there is local data (team) without an associated user ID.
     * @return true if there is a team without a coachId, false otherwise
     */
    suspend fun hasLocalTeamWithoutUserId(): Boolean

    /**
     * Get team directly (not as a Flow) for migration purposes.
     * @return Team if exists, null otherwise
     */
    suspend fun getTeamDirect(): Team?

    /**
     * Clear all team data from local storage.
     * Only applicable for local data sources.
     */
    suspend fun clearLocalData()

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
}
