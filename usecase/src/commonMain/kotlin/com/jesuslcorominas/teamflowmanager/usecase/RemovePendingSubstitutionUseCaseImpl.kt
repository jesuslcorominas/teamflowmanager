package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePendingSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PendingSubstitutionsRepository

internal class RemovePendingSubstitutionUseCaseImpl(
    private val pendingSubstitutionsRepository: PendingSubstitutionsRepository,
) : RemovePendingSubstitutionUseCase {
    override fun invoke(
        matchId: String,
        pair: SubstitutionPair,
    ) {
        pendingSubstitutionsRepository.remove(matchId, pair)
    }
}
