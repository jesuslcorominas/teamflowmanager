package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair

/** Unschedules exactly [pair] from [matchId]. Does nothing when it is not scheduled. */
interface RemovePendingSubstitutionUseCase {
    operator fun invoke(
        matchId: String,
        pair: SubstitutionPair,
    )
}
