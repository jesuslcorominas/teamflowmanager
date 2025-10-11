package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.Flow

interface GetMatchUseCase {
    operator fun invoke(): Flow<Match?>
}

internal class GetMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
) : GetMatchUseCase {
    override fun invoke(): Flow<Match?> = matchRepository.getMatch()
}
