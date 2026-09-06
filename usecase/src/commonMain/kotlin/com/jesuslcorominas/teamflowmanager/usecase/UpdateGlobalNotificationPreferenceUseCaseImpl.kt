package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateGlobalNotificationPreferenceUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.first

internal class UpdateGlobalNotificationPreferenceUseCaseImpl(
    private val authRepository: AuthRepository,
    private val notificationPreferencesRepository: NotificationPreferencesRepository,
) : UpdateGlobalNotificationPreferenceUseCase {
    override suspend fun invoke(
        clubId: String,
        type: NotificationEventType,
        enabled: Boolean,
    ) {
        val user = authRepository.getCurrentUser().first() ?: return
        notificationPreferencesRepository.updateGlobalPreference(user.id, clubId, type, enabled)
    }
}
