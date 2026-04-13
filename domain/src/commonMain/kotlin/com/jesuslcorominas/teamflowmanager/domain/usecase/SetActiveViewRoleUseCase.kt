package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole

interface SetActiveViewRoleUseCase {
    operator fun invoke(role: ActiveViewRole)
}
