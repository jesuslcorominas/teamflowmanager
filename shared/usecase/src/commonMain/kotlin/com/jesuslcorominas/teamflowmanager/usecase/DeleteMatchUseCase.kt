package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface DeleteMatchUseCase {
    suspend operator fun invoke(matchId: Long)
}

internal class DeleteMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
) : DeleteMatchUseCase {
    override suspend fun invoke(matchId: Long) {
        matchRepository.deleteMatch(matchId)
    }
}
