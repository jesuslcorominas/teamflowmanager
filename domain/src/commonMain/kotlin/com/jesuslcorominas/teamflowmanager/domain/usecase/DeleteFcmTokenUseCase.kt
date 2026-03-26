package com.jesuslcorominas.teamflowmanager.domain.usecase

interface DeleteFcmTokenUseCase {
    suspend operator fun invoke(userId: String)
}
