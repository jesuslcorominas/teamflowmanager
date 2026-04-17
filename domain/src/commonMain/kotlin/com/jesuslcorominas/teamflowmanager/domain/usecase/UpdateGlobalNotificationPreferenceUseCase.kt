package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType

interface UpdateGlobalNotificationPreferenceUseCase {
    suspend operator fun invoke(
        clubId: String,
        type: NotificationEventType,
        enabled: Boolean,
        allTeamRemoteIds: List<String>,
    )
}
