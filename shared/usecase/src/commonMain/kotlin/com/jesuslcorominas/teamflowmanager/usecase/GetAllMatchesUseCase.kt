package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.Flow

interface GetAllMatchesUseCase {
    operator fun invoke(): Flow<List<Match>>
}

internal class GetAllMatchesUseCaseImpl(
    private val matchRepository: MatchRepository,
) : GetAllMatchesUseCase {
    override fun invoke(): Flow<List<Match>> = matchRepository.getAllMatches()
}
