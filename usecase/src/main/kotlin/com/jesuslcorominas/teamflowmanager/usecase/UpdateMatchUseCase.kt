package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface UpdateMatchUseCase {
    suspend operator fun invoke(match: Match)
}

internal class UpdateMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
) : UpdateMatchUseCase {
    override suspend fun invoke(match: Match) {
        matchRepository.updateMatch(match)
    }
}
