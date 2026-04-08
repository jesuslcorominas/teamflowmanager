package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPresidentNotificationsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository
import kotlinx.coroutines.flow.Flow

class GetPresidentNotificationsUseCaseImpl(
    private val repository: PresidentNotificationRepository,
) : GetPresidentNotificationsUseCase {
    override fun invoke(clubId: String): Flow<List<PresidentNotification>> = repository.getNotifications(clubId)
}
