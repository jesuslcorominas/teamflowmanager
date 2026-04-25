package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchDataSource {
    fun getMatchById(matchId: Long, teamId: String? = null): Flow<Match?>

    fun getAllMatches(): Flow<List<Match>>

    fun getMatchesByTeam(teamId: String): Flow<List<Match>>

    fun getArchivedMatches(): Flow<List<Match>>

    suspend fun getScheduledMatches(): List<Match>

    suspend fun updateMatchCaptain(
        matchId: Long,
        captainId: Long?,
    )

    suspend fun insertMatch(match: Match): Long

    suspend fun updateMatch(match: Match)

    suspend fun deleteMatch(matchId: Long)

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
}
