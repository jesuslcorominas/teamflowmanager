package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

internal class DeleteMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
) : DeleteMatchUseCase {
    override suspend fun invoke(matchId: Long) {
        matchRepository.deleteMatch(matchId)
    }
}
