package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole

interface GetActiveViewRoleUseCase {
    operator fun invoke(): ActiveViewRole
}
