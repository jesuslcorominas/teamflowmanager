package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPendingSubstitutionConflictsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PendingSubstitutionsRepository

internal class GetPendingSubstitutionConflictsUseCaseImpl(
    private val pendingSubstitutionsRepository: PendingSubstitutionsRepository,
) : GetPendingSubstitutionConflictsUseCase {
    override fun invoke(
        matchId: String,
        pair: SubstitutionPair,
    ): List<SubstitutionPair> = pendingSubstitutionsRepository.conflictsFor(matchId, pair)
}
