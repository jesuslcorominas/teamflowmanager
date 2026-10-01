package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.AddPendingSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PendingSubstitutionsRepository

internal class AddPendingSubstitutionUseCaseImpl(
    private val pendingSubstitutionsRepository: PendingSubstitutionsRepository,
) : AddPendingSubstitutionUseCase {
    override fun invoke(
        matchId: String,
        pair: SubstitutionPair,
    ) {
        pendingSubstitutionsRepository.add(matchId, pair)
    }
}
