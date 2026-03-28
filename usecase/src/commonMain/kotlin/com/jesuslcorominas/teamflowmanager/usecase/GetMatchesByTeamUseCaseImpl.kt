package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchesByTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.Flow

internal class GetMatchesByTeamUseCaseImpl(
    private val matchRepository: MatchRepository,
) : GetMatchesByTeamUseCase {
    override fun invoke(teamFirestoreId: String): Flow<List<Match>> =
        matchRepository.getMatchesByTeam(teamFirestoreId)
}
