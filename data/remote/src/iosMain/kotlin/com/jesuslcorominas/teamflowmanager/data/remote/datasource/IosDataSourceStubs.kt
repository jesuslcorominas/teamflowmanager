package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.DynamicLinkDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ImageStorageDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationPreferencesDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PresidentNotificationDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// ── Stub datasources for iOS Phase 2 MVP ─────────────────────────────────────
// Write operations and local-only operations throw NotImplementedError.
// Read operations return safe empty/null defaults so the app navigates normally
// when no data is present.

class ClubFirestoreDataSourceImpl : ClubDataSource {
    override suspend fun createClubWithOwner(
        clubName: String,
        currentUserId: String,
        currentUserName: String,
        currentUserEmail: String,
    ): Club = throw NotImplementedError("createClubWithOwner not implemented for iOS Phase 2")

    override suspend fun getClubByInvitationCode(invitationCode: String): Club? = null
}

class NoOpImageStorageDataSource : ImageStorageDataSource {
    override suspend fun uploadImage(
        localUri: String,
        path: String,
    ): String? = null

    override suspend fun deleteImage(downloadUrl: String): Boolean = false
}

class NoOpDynamicLinkDataSource : DynamicLinkDataSource {
    override suspend fun generateTeamInvitationLink(
        teamId: String,
        teamName: String,
    ): String = throw NotImplementedError("generateTeamInvitationLink not implemented for iOS Phase 2")
}

class PresidentNotificationDataSourceStub : PresidentNotificationDataSource {
    override fun getNotifications(clubId: String): Flow<List<PresidentNotification>> = flowOf(emptyList())

    override fun getUnreadCount(clubId: String): Flow<Int> = flowOf(0)

    override suspend fun markAsRead(
        clubId: String,
        notificationId: String,
    ): Unit = throw NotImplementedError("markAsRead not implemented for iOS Phase 2")

    override suspend fun markAsUnread(
        clubId: String,
        notificationId: String,
    ): Unit = throw NotImplementedError("markAsUnread not implemented for iOS Phase 2")

    override suspend fun deleteNotification(
        clubId: String,
        notificationId: String,
    ): Unit = throw NotImplementedError("deleteNotification not implemented for iOS Phase 2")
}

class NotificationPreferencesStubDataSourceImpl : NotificationPreferencesDataSource {
    override fun getPreferences(
        userId: String,
        clubId: String,
    ): Flow<UserNotificationPreferences> = flowOf(UserNotificationPreferences(userId = userId))

    override suspend fun updateGlobalPreference(
        userId: String,
        clubId: String,
        type: NotificationEventType,
        enabled: Boolean,
        allTeamRemoteIds: List<String>,
    ) {
        // stub — no-op on iOS
    }

    override suspend fun updateTeamPreference(
        userId: String,
        clubId: String,
        teamRemoteId: String,
        type: NotificationEventType,
        enabled: Boolean,
    ) {
        // stub — no-op on iOS
    }
}
