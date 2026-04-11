package com.jesuslcorominas.teamflowmanager.domain.usecase

interface DeletePresidentNotificationUseCase {
    suspend operator fun invoke(
        clubId: String,
        notificationId: String,
    )
}
