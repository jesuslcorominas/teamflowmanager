package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.Flow



internal class GetMatchByIdUseCaseImpl(
    private val matchRepository: MatchRepository,
) : GetMatchByIdUseCase {
    override fun invoke(matchId: Long): Flow<Match?> = matchRepository.getMatchById(matchId)
}
