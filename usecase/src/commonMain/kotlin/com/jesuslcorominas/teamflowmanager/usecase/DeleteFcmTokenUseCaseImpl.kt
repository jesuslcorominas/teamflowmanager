package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenProviderRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenRepository

class DeleteFcmTokenUseCaseImpl(
    private val fcmTokenProviderRepository: FcmTokenProviderRepository,
    private val fcmTokenRepository: FcmTokenRepository,
) : DeleteFcmTokenUseCase {
    override suspend fun invoke(userId: String) {
        val token = fcmTokenProviderRepository.getToken()
        if (token.isNotEmpty()) {
            fcmTokenRepository.deleteToken(userId, token)
        }
    }
}
