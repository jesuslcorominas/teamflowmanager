package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObservePendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PendingSubstitutionsRepository
import kotlinx.coroutines.flow.Flow

internal class ObservePendingSubstitutionsUseCaseImpl(
    private val pendingSubstitutionsRepository: PendingSubstitutionsRepository,
) : ObservePendingSubstitutionsUseCase {
    override fun invoke(matchId: String): Flow<List<SubstitutionPair>> = pendingSubstitutionsRepository.observe(matchId)
}
