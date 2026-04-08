package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.MarkPresidentNotificationAsUnreadUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository

class MarkPresidentNotificationAsUnreadUseCaseImpl(
    private val repository: PresidentNotificationRepository,
) : MarkPresidentNotificationAsUnreadUseCase {
    override suspend fun invoke(
        clubId: String,
        notificationId: String,
    ) {
        repository.markAsUnread(clubId, notificationId)
    }
}
