package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

interface SetNotificationPermissionRequestedUseCase {
    operator fun invoke(requested: Boolean)
}

internal class SetNotificationPermissionRequestedUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : SetNotificationPermissionRequestedUseCase {
    override fun invoke(requested: Boolean) {
        preferencesRepository.setNotificationPermissionRequested(requested)
    }
}
