package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.IsNotificationPermissionGrantedUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPermissionRepository

class IsNotificationPermissionGrantedUseCaseImpl(
    private val repository: NotificationPermissionRepository,
) : IsNotificationPermissionGrantedUseCase {
    override fun invoke(): Boolean = repository.isGranted()
}
