package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetArchivedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.Flow

internal class GetArchivedMatchesUseCaseImpl(
    private val matchRepository: MatchRepository,
) : GetArchivedMatchesUseCase {
    override fun invoke(): Flow<List<Match>> = matchRepository.getArchivedMatches()
}
