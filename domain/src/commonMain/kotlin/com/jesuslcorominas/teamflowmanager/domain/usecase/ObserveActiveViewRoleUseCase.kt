package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import kotlinx.coroutines.flow.Flow

/**
 * Emits the active view role and every later change to it.
 *
 * The one-shot [GetActiveViewRoleUseCase] is still the right tool where the role is read once, but
 * anything that must *stay* in sync with it — the app shell deciding which navigation to show —
 * needs to observe it: switching role writes to local storage, which no other flow reports.
 */
interface ObserveActiveViewRoleUseCase {
    operator fun invoke(): Flow<ActiveViewRole>
}
