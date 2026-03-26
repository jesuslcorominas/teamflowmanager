package com.jesuslcorominas.teamflowmanager.domain.usecase

interface SyncFcmTokenUseCase {
    suspend operator fun invoke(userId: String, platform: String, clubFirestoreId: String?)
}
