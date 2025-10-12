package com.jesuslcorominas.teamflowmanager.domain.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Match

interface MatchRepository {
    suspend fun getMatchById(matchId: String): Match?
    suspend fun updateMatch(match: Match): Result<Unit>
    suspend fun pauseMatch(matchId: String): Result<Match>
    suspend fun resumeMatch(matchId: String): Result<Match>
}
