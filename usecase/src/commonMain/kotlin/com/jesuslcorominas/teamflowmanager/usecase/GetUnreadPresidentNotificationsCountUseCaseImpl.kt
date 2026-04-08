package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUnreadPresidentNotificationsCountUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository
import kotlinx.coroutines.flow.Flow

class GetUnreadPresidentNotificationsCountUseCaseImpl(
    private val repository: PresidentNotificationRepository,
) : GetUnreadPresidentNotificationsCountUseCase {
    override fun invoke(clubId: String): Flow<Int> = repository.getUnreadCount(clubId)
}
