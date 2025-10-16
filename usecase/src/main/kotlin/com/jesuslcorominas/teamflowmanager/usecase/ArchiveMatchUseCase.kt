package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

interface ArchiveMatchUseCase {
    suspend operator fun invoke(matchId: Long)
}

internal class ArchiveMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
) : ArchiveMatchUseCase {
    override suspend fun invoke(matchId: Long) {
        matchRepository.archiveMatch(matchId)
    }
}
