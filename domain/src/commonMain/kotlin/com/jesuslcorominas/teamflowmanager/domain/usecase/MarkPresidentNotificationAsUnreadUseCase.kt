package com.jesuslcorominas.teamflowmanager.domain.usecase

interface MarkPresidentNotificationAsUnreadUseCase {
    suspend operator fun invoke(
        clubId: String,
        notificationId: String,
    )
}
