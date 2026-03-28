package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenProviderRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationTopicRepository

class DeleteFcmTokenUseCaseImpl(
    private val fcmTokenProviderRepository: FcmTokenProviderRepository,
    private val fcmTokenRepository: FcmTokenRepository,
    private val notificationTopicRepository: NotificationTopicRepository,
) : DeleteFcmTokenUseCase {
    override suspend fun invoke(userId: String) {
        val token = fcmTokenProviderRepository.getToken()
        if (token.isEmpty()) return

        // Read stored topic before deleting so we can unsubscribe
        val entry = fcmTokenRepository.getTokenEntry(userId, token)
        entry?.topic?.let { notificationTopicRepository.unsubscribeFromRawTopic(it) }

        fcmTokenRepository.deleteToken(userId, token)
    }
}
