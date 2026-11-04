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
     * Updates the match with a specific operation ID to track atomic operations.
     *
     * @deprecated Use [updateLastCompletedOperationId] instead. That method does a partial field
     * update and avoids overwriting concurrent writes on other fields (#417).
     */
    @Deprecated(
        message = "Use updateLastCompletedOperationId for partial field update to avoid lost updates (#417).",
        replaceWith = ReplaceWith("updateLastCompletedOperationId(match.id, operationId)"),
    )
    suspend fun updateMatchWithOperationId(
        match: Match,
        operationId: String,
    )

    /**
     * Writes only the [Match.lastCompletedOperationId] field, leaving all other fields untouched.
     *
     * This is a targeted partial-field update — not a full document replace — so it cannot
     * overwrite concurrent writes on other fields (e.g. a goal registered from another device at
     * the same time). Callers must prefer this over [updateMatch] whenever the only intent is to
     * advance the completed operation pointer (#417).
     *
     * @param matchId     The ID of the match to update.
     * @param operationId The operation ID to write into [Match.lastCompletedOperationId].
     */
    suspend fun updateLastCompletedOperationId(
        matchId: String,
        operationId: String,
    )
}
