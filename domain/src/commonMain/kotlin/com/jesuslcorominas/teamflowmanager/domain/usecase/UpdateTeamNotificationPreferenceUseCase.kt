package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType

interface UpdateTeamNotificationPreferenceUseCase {
    suspend operator fun invoke(
        clubId: String,
        teamRemoteId: String,
        type: NotificationEventType,
        enabled: Boolean,
    )
}
