package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SyncFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenProviderRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenRepository

class SyncFcmTokenUseCaseImpl(
    private val fcmTokenProviderRepository: FcmTokenProviderRepository,
    private val fcmTokenRepository: FcmTokenRepository,
) : SyncFcmTokenUseCase {
    override suspend fun invoke(userId: String, platform: String) {
        val token = fcmTokenProviderRepository.getToken()
        if (token.isNotEmpty()) {
            fcmTokenRepository.saveToken(userId, token, platform)
        }
    }
}
