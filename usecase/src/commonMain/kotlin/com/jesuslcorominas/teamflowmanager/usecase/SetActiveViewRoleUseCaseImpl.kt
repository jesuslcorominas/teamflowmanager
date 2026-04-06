package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

internal class SetActiveViewRoleUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : SetActiveViewRoleUseCase {
    override fun invoke(role: ActiveViewRole) {
        val value =
            when (role) {
                ActiveViewRole.Coach -> GetActiveViewRoleUseCaseImpl.ROLE_COACH
                ActiveViewRole.President -> GetActiveViewRoleUseCaseImpl.ROLE_PRESIDENT
            }
        preferencesRepository.setActiveViewRole(value)
    }
}
