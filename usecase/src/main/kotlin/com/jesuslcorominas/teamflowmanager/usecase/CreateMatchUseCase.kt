package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface CreateMatchUseCase {
    suspend operator fun invoke(match: Match): Long
}

internal class CreateMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
) : CreateMatchUseCase {
    override suspend fun invoke(match: Match): Long = matchRepository.createMatch(match)
}
