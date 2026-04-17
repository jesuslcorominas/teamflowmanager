package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationPreferencesDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.Flow

class NotificationPreferencesRepositoryImpl(
    private val dataSource: NotificationPreferencesDataSource,
) : NotificationPreferencesRepository {
    override fun getPreferences(
        userId: String,
        clubId: String,
    ): Flow<UserNotificationPreferences> = dataSource.getPreferences(userId, clubId)

    override suspend fun updateGlobalPreference(
        userId: String,
        clubId: String,
        type: NotificationEventType,
        enabled: Boolean,
        allTeamRemoteIds: List<String>,
    ) = dataSource.updateGlobalPreference(userId, clubId, type, enabled, allTeamRemoteIds)

    override suspend fun updateTeamPreference(
        userId: String,
        clubId: String,
        teamRemoteId: String,
        type: NotificationEventType,
        enabled: Boolean,
    ) = dataSource.updateTeamPreference(userId, clubId, teamRemoteId, type, enabled)
}
