package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

interface HasNotificationPermissionBeenRequestedUseCase {
    operator fun invoke(): Boolean
}

internal class HasNotificationPermissionBeenRequestedUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : HasNotificationPermissionBeenRequestedUseCase {
    override fun invoke(): Boolean {
        return preferencesRepository.hasNotificationPermissionBeenRequested()
    }
}
