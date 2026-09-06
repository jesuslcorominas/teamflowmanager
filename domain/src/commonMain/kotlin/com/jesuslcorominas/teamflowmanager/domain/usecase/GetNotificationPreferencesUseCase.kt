package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences
import kotlinx.coroutines.flow.Flow

interface GetNotificationPreferencesUseCase {
    operator fun invoke(clubId: String): Flow<UserNotificationPreferences>
}
