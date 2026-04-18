package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateTeamNotificationPreferenceUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.first

internal class UpdateTeamNotificationPreferenceUseCaseImpl(
    private val authRepository: AuthRepository,
    private val notificationPreferencesRepository: NotificationPreferencesRepository,
) : UpdateTeamNotificationPreferenceUseCase {
    override suspend fun invoke(
        clubId: String,
        teamRemoteId: String,
        type: NotificationEventType,
        enabled: Boolean,
    ) {
        val user = authRepository.getCurrentUser().first() ?: return
        notificationPreferencesRepository.updateTeamPreference(user.id, clubId, teamRemoteId, type, enabled)
    }
}
