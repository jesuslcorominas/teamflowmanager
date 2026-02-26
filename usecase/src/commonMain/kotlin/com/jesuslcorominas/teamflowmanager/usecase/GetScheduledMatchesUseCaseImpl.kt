package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

internal class GetScheduledMatchesUseCaseImpl(
    private val matchRepository: MatchRepository,
) : GetScheduledMatchesUseCase {
    override suspend fun invoke(): List<Match> {
        return matchRepository.getScheduledMatches()
    }
}
