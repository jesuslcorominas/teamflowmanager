package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences
import kotlinx.coroutines.flow.Flow

interface NotificationPreferencesDataSource {
    fun getPreferences(
        userId: String,
        clubId: String,
    ): Flow<UserNotificationPreferences>

    suspend fun updateGlobalPreference(
        userId: String,
        clubId: String,
        type: NotificationEventType,
        enabled: Boolean,
    )

    suspend fun updateTeamPreference(
        userId: String,
        clubId: String,
        teamRemoteId: String,
        type: NotificationEventType,
        enabled: Boolean,
    )
}
