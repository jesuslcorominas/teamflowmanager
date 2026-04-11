package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.UnsubscribeFromClubNotificationsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationTopicRepository

class UnsubscribeFromClubNotificationsUseCaseImpl(
    private val notificationTopicRepository: NotificationTopicRepository,
) : UnsubscribeFromClubNotificationsUseCase {
    override suspend fun invoke(clubId: String) = notificationTopicRepository.unsubscribeFromClub(clubId)
}
