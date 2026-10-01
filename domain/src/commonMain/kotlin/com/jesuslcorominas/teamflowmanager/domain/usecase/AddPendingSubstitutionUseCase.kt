package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair

/**
 * Schedules [pair] for [matchId], discarding any pair already scheduled that shares one of its
 * players. Use [GetPendingSubstitutionConflictsUseCase] to know which ones beforehand.
 */
interface AddPendingSubstitutionUseCase {
    operator fun invoke(
        matchId: String,
        pair: SubstitutionPair,
    )
}
