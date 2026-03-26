package com.jesuslcorominas.teamflowmanager.domain.usecase

interface UnsubscribeFromClubNotificationsUseCase {
    suspend operator fun invoke(clubFirestoreId: String)
}
