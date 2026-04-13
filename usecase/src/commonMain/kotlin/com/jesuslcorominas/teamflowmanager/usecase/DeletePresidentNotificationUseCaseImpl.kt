package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.DeletePresidentNotificationUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository

class DeletePresidentNotificationUseCaseImpl(
    private val repository: PresidentNotificationRepository,
) : DeletePresidentNotificationUseCase {
    override suspend fun invoke(
        clubId: String,
        notificationId: String,
    ) {
        repository.deleteNotification(clubId, notificationId)
    }
}
