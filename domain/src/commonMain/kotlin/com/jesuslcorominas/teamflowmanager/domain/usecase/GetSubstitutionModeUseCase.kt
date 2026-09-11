package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import kotlinx.coroutines.flow.Flow

/**
 * Emits the substitution mode in effect and every later change to it.
 *
 * Observable rather than one-shot: key-value storage reports no changes, so anything that must
 * stay in sync with the setting — the Settings switch, and later the match screen — needs the flow.
 * Emits [SubstitutionMode.SCHEDULED] when nothing has been stored.
 */
interface GetSubstitutionModeUseCase {
    operator fun invoke(): Flow<SubstitutionMode>
}
