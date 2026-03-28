package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SubscribeToClubNotificationsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationTopicRepository

class SubscribeToClubNotificationsUseCaseImpl(
    private val notificationTopicRepository: NotificationTopicRepository,
) : SubscribeToClubNotificationsUseCase {
    override suspend fun invoke(clubFirestoreId: String) = notificationTopicRepository.subscribeToClub(clubFirestoreId)
}
