package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchDataSource {
    fun getMatchById(matchId: String): Flow<Match?>

    fun getAllMatches(): Flow<List<Match>>

    fun getMatchesByTeam(teamId: String): Flow<List<Match>>

    fun getArchivedMatches(): Flow<List<Match>>

    suspend fun getScheduledMatches(): List<Match>

    suspend fun updateMatchCaptain(
        matchId: String,
        captainId: String?,
    )

    suspend fun insertMatch(match: Match): String

    suspend fun updateMatch(match: Match)

    suspend fun deleteMatch(matchId: String)

    /**
     * Get all matches directly (not as a Flow) for migration purposes.
     * @return List of all matches
     */
    suspend fun getAllMatchesDirect(): List<Match>

    /**
     * Clear all match data from local storage.
     * Only applicable for local data sources.
     */
    suspend fun clearLocalData()

    /**
     * Writes only the [Match.lastCompletedOperationId] field, leaving all other fields untouched.
     *
     * This is a targeted partial-field update — not a full document replace — so it cannot
     * overwrite concurrent writes on other fields (#417).
     */
    suspend fun updateLastCompletedOperationId(
        matchId: String,
        operationId: String,
    )
}
