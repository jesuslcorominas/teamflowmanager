package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.HasScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository


internal class HasScheduledMatchesUseCaseImpl(
    private val matchRepository: MatchRepository,
) : HasScheduledMatchesUseCase {
    override suspend fun invoke(): Boolean {
        return matchRepository.getScheduledMatches().isNotEmpty()
    }
}
