package com.jesuslcorominas.teamflowmanager.domain.usecase

interface SubscribeToClubNotificationsUseCase {
    suspend operator fun invoke(clubFirestoreId: String)
}
