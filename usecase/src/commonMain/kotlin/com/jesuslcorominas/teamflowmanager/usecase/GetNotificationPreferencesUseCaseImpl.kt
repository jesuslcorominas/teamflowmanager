package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetNotificationPreferencesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

internal class GetNotificationPreferencesUseCaseImpl(
    private val authRepository: AuthRepository,
    private val notificationPreferencesRepository: NotificationPreferencesRepository,
) : GetNotificationPreferencesUseCase {
    override fun invoke(clubId: String): Flow<UserNotificationPreferences> =
        flow {
            val user = authRepository.getCurrentUser().first() ?: return@flow
            emitAll(notificationPreferencesRepository.getPreferences(user.id, clubId))
        }
}
