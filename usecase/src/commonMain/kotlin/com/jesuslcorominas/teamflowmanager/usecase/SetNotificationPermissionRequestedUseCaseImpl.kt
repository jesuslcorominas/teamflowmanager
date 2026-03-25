package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SetNotificationPermissionRequestedUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

internal class SetNotificationPermissionRequestedUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : SetNotificationPermissionRequestedUseCase {
    override fun invoke(requested: Boolean) {
        preferencesRepository.setNotificationPermissionRequested(requested)
    }
}
