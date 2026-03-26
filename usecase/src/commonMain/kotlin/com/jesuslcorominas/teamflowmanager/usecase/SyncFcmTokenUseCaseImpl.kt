package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SyncFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenProviderRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationTopicRepository

class SyncFcmTokenUseCaseImpl(
    private val fcmTokenProviderRepository: FcmTokenProviderRepository,
    private val fcmTokenRepository: FcmTokenRepository,
    private val notificationTopicRepository: NotificationTopicRepository,
) : SyncFcmTokenUseCase {
    override suspend fun invoke(userId: String, platform: String, clubFirestoreId: String?) {
        val token = fcmTokenProviderRepository.getToken()
        if (token.isEmpty()) return

        // Cleanup: remove tokens of other users on this device and unsubscribe their topics
        val othersTokens = fcmTokenRepository.findTokensForOtherUsers(token, userId)
        for (entry in othersTokens) {
            entry.topic?.let { notificationTopicRepository.unsubscribeFromRawTopic(it) }
            fcmTokenRepository.deleteToken(entry.userId, token)
        }

        // Save current user token with their club topic
        val topic = clubFirestoreId?.let { "club_$it" }
        fcmTokenRepository.saveToken(userId, token, platform, topic)

        // Subscribe to club topic if present
        if (clubFirestoreId != null) {
            notificationTopicRepository.subscribeToClub(clubFirestoreId)
        }
    }
}
