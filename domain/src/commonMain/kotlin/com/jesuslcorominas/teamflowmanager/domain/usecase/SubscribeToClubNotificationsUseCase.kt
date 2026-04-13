package com.jesuslcorominas.teamflowmanager.domain.usecase

interface SubscribeToClubNotificationsUseCase {
    suspend operator fun invoke(clubId: String)
}
