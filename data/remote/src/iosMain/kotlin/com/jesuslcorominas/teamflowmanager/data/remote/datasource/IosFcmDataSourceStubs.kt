package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenProviderDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationPermissionDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationTopicDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.FcmTokenEntry
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload

// iOS stubs — replace with real implementations when APNs is configured in Firebase Console

class IosFcmDataSourceImpl : FcmDataSource {
    override suspend fun saveToken(
        userId: String,
        token: String,
        platform: String,
        topic: String?,
    ) {}

    override suspend fun deleteToken(
        userId: String,
        token: String,
    ) {}

    override suspend fun getTokenEntry(
        userId: String,
        token: String,
    ): FcmTokenEntry? = null

    override suspend fun findTokensForOtherUsers(
        token: String,
        currentUserId: String,
    ): List<FcmTokenEntry> = emptyList()

    override suspend fun getTokensByUserId(userId: String): List<String> = emptyList()

    override suspend fun sendNotification(
        token: String,
        payload: NotificationPayload,
    ) {}
}

class IosFcmTokenProviderDataSourceImpl : FcmTokenProviderDataSource {
    override suspend fun getToken(): String = ""
}

class IosNotificationTopicDataSourceImpl : NotificationTopicDataSource {
    override suspend fun subscribe(topic: String) {}

    override suspend fun unsubscribe(topic: String) {}
}

// iOS stub — APNs not configured yet. Return true so the login flow proceeds without
// blocking on the Android-only POST_NOTIFICATIONS permission dialog.
class IosNotificationPermissionDataSourceImpl : NotificationPermissionDataSource {
    override fun isGranted(): Boolean = true
}
