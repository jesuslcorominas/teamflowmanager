package com.jesuslcorominas.teamflowmanager.usecase.match

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.repository.MatchRepository

class GetMatchUseCase(
    private val matchRepository: MatchRepository
) {
    suspend operator fun invoke(matchId: String): Match? {
        return matchRepository.getMatchById(matchId)
    }
}
