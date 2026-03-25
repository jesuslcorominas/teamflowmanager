package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.ArchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository

internal class ArchiveMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
) : ArchiveMatchUseCase {
    override suspend fun invoke(matchId: Long) {
        matchRepository.archiveMatch(matchId)
    }
}
