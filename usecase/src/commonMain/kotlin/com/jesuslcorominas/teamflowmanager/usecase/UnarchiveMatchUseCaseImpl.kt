package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.UnarchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

internal class UnarchiveMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
) : UnarchiveMatchUseCase {
    override suspend fun invoke(matchId: Long) {
        matchRepository.unarchiveMatch(matchId)
    }
}
