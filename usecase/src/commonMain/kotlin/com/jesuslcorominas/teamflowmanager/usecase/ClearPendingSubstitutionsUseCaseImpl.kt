package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.ClearPendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PendingSubstitutionsRepository

internal class ClearPendingSubstitutionsUseCaseImpl(
    private val pendingSubstitutionsRepository: PendingSubstitutionsRepository,
) : ClearPendingSubstitutionsUseCase {
    override fun invoke(matchId: String) {
        pendingSubstitutionsRepository.clear(matchId)
    }
}
