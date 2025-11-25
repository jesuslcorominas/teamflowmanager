package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface HasScheduledMatchesUseCase {
    suspend operator fun invoke(): Boolean
}

internal class HasScheduledMatchesUseCaseImpl(
    private val matchRepository: MatchRepository,
) : HasScheduledMatchesUseCase {
    override suspend fun invoke(): Boolean {
        return matchRepository.getScheduledMatches().isNotEmpty()
    }
}
