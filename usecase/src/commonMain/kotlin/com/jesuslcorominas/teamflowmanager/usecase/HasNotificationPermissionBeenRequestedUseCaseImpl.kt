package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.HasNotificationPermissionBeenRequestedUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository


internal class HasNotificationPermissionBeenRequestedUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : HasNotificationPermissionBeenRequestedUseCase {
    override fun invoke(): Boolean {
        return preferencesRepository.hasNotificationPermissionBeenRequested()
    }
}
