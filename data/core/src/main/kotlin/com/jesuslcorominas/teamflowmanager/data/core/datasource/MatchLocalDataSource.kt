package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchLocalDataSource {
    fun getMatch(): Flow<Match?>

    fun getMatchById(matchId: Long): Flow<Match?>

    fun getAllMatches(): Flow<List<Match>>

    fun getArchivedMatches(): Flow<List<Match>>

    suspend fun getScheduledMatches(): List<Match>

    suspend fun updateMatchCaptain(matchId: Long, captainId: Long?)

    suspend fun upsertMatch(match: Match)

    suspend fun insertMatch(match: Match): Long

    suspend fun updateMatch(match: Match)

    suspend fun deleteMatch(matchId: Long)
}
