package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.repository.MatchRepository
import kotlinx.coroutines.delay
import java.util.Date

class MatchRepositoryImpl : MatchRepository {
    private val matches = mutableMapOf<String, Match>()

    override suspend fun getMatchById(matchId: String): Match? {
        return matches[matchId]
    }

    override suspend fun updateMatch(match: Match): Result<Unit> {
        return try {
            matches[match.id] = match
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pauseMatch(matchId: String): Result<Match> {
        return try {
            val match = matches[matchId]
                ?: return Result.failure(Exception("Match not found"))

            if (match.status != MatchStatus.IN_PROGRESS) {
                return Result.failure(Exception("Match is not in progress"))
            }

            val pausedMatch = match.copy(
                status = MatchStatus.PAUSED,
                activePlayerTimers = match.activePlayerTimers.map { 
                    it.copy(isActive = false) 
                }
            )
            
            matches[matchId] = pausedMatch
            Result.success(pausedMatch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resumeMatch(matchId: String): Result<Match> {
        return try {
            val match = matches[matchId]
                ?: return Result.failure(Exception("Match not found"))

            if (match.status != MatchStatus.PAUSED) {
                return Result.failure(Exception("Match is not paused"))
            }

            val resumedMatch = match.copy(
                status = MatchStatus.IN_PROGRESS
            )
            
            matches[matchId] = resumedMatch
            Result.success(resumedMatch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
