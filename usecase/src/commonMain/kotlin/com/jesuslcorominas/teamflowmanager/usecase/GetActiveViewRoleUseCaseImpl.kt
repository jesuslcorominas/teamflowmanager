package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

internal class GetActiveViewRoleUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : GetActiveViewRoleUseCase {
    override fun invoke(): ActiveViewRole =
        when (preferencesRepository.getActiveViewRole()) {
            ROLE_COACH -> ActiveViewRole.Coach
            else -> ActiveViewRole.President
        }

    companion object {
        const val ROLE_COACH = "coach"
        const val ROLE_PRESIDENT = "president"
    }
}
