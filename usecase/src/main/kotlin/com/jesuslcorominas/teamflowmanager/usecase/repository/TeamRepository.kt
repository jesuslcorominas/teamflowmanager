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
     * Check if there is local data (team) without an associated user ID.
     * This is useful for detecting data created before user authentication was added.
     * @return true if there is a team without a coachId, false otherwise
     */
    suspend fun hasLocalTeamWithoutUserId(): Boolean
}
