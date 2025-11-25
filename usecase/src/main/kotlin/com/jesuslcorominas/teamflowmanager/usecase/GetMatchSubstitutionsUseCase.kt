package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import kotlinx.coroutines.flow.Flow

interface GetMatchSubstitutionsUseCase {
    operator fun invoke(matchId: Long): Flow<List<PlayerSubstitution>>
}

internal class GetMatchSubstitutionsUseCaseImpl(
    private val playerSubstitutionRepository: PlayerSubstitutionRepository,
) : GetMatchSubstitutionsUseCase {
    override fun invoke(matchId: Long): Flow<List<PlayerSubstitution>> {
        return playerSubstitutionRepository.getMatchSubstitutions(matchId)
    }
}
