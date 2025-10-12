package com.jesuslcorominas.teamflowmanager.usecase.match

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.repository.MatchRepository

class PauseMatchUseCase(
    private val matchRepository: MatchRepository
) {
    suspend operator fun invoke(matchId: String): Result<Match> {
        return try {
            val match = matchRepository.getMatchById(matchId)
                ?: return Result.failure(Exception("Match not found"))

            if (match.status != MatchStatus.IN_PROGRESS) {
                return Result.failure(Exception("Match is not in progress"))
            }

            matchRepository.pauseMatch(matchId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
