package com.jesuslcorominas.teamflowmanager.domain.usecase

interface SetNotificationPermissionRequestedUseCase {
    operator fun invoke(requested: Boolean)
}
