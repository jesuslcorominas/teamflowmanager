package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getMatchById(matchId: String): Flow<Match?>

    fun getAllMatches(): Flow<List<Match>>

    fun getMatchesByTeam(teamId: String): Flow<List<Match>>

    fun getArchivedMatches(): Flow<List<Match>>

    suspend fun getScheduledMatches(): List<Match>

    suspend fun updateMatchCaptain(
        matchId: String,
        captainId: String?,
    )

    suspend fun createMatch(match: Match): String

    suspend fun updateMatch(match: Match)

    suspend fun deleteMatch(matchId: String)

    suspend fun startTimer(
        matchId: String,
        currentTimeMillis: Long,
    )

    suspend fun pauseTimer(
        matchId: String,
        currentTimeMillis: Long,
    )

    suspend fun startTimeout(
        matchId: String,
        currentTimeMillis: Long,
    )

    suspend fun endTimeout(
        matchId: String,
        currentTimeMillis: Long,
    )

    suspend fun archiveMatch(matchId: String)

    suspend fun unarchiveMatch(matchId: String)

    /**
     * Updates the match with a specific operation ID to track atomic operations
     */
    suspend fun updateMatchWithOperationId(
        match: Match,
        operationId: String,
    )
}
