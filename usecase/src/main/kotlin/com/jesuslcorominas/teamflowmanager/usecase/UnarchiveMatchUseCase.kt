package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface UnarchiveMatchUseCase {
    suspend operator fun invoke(matchId: Long)
}

internal class UnarchiveMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
) : UnarchiveMatchUseCase {
    override suspend fun invoke(matchId: Long) {
        matchRepository.unarchiveMatch(matchId)
    }
}
