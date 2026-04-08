package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.MarkPresidentNotificationAsReadUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository

class MarkPresidentNotificationAsReadUseCaseImpl(
    private val repository: PresidentNotificationRepository,
) : MarkPresidentNotificationAsReadUseCase {
    override suspend fun invoke(
        clubId: String,
        notificationId: String,
    ) {
        repository.markAsRead(clubId, notificationId)
    }
}
