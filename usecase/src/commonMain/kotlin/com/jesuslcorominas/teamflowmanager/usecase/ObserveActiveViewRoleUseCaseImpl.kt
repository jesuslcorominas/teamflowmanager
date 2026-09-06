package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObserveActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ObserveActiveViewRoleUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : ObserveActiveViewRoleUseCase {
    override fun invoke(): Flow<ActiveViewRole> =
        preferencesRepository.observeActiveViewRole().map { stored ->
            when (stored) {
                GetActiveViewRoleUseCaseImpl.ROLE_COACH -> ActiveViewRole.Coach
                else -> ActiveViewRole.President
            }
        }
}
