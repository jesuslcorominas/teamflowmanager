package com.jesuslcorominas.teamflowmanager.domain.usecase

interface MarkPresidentNotificationAsReadUseCase {
    suspend operator fun invoke(
        clubId: String,
        notificationId: String,
    )
}
