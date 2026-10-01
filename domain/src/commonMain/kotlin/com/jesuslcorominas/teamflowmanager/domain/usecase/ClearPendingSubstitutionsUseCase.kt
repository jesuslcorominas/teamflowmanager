package com.jesuslcorominas.teamflowmanager.domain.usecase

/** Unschedules every pending substitution of [matchId]. Leaves other matches untouched. */
interface ClearPendingSubstitutionsUseCase {
    operator fun invoke(matchId: String)
}
