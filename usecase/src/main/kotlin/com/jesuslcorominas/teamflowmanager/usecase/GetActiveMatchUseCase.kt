package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface GetActiveMatchUseCase {
    operator fun invoke(): Flow<Match?>
}

internal class GetActiveMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
) : GetActiveMatchUseCase {
    override fun invoke(): Flow<Match?> = matchRepository.getAllMatches().map { matches ->
        matches.firstOrNull { match ->
            match.status == MatchStatus.IN_PROGRESS ||
                match.status == MatchStatus.PAUSED ||
                match.status == MatchStatus.TIMEOUT
        }
    }
}
