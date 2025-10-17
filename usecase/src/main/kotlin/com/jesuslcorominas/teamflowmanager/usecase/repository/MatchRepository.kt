package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getMatch(): Flow<Match?>

    fun getMatchById(matchId: Long): Flow<Match?>

    fun getAllMatches(): Flow<List<Match>>

    fun getArchivedMatches(): Flow<List<Match>>

    suspend fun getScheduledMatches(): List<Match>

    suspend fun updateMatchCaptain(matchId: Long, captainId: Long?)

    suspend fun createMatch(match: Match): Long

    suspend fun updateMatch(match: Match)

    suspend fun deleteMatch(matchId: Long)

    suspend fun startTimer(currentTimeMillis: Long)

    suspend fun pauseTimer(currentTimeMillis: Long)

    suspend fun archiveMatch(matchId: Long)

    suspend fun unarchiveMatch(matchId: Long)
}
