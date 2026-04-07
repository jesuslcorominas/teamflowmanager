package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmSendNotificationDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenProviderDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationPermissionDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationTopicDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.FcmTokenEntry

// iOS stubs — replace with real implementations when APNs is configured in Firebase Console

class IosFcmTokenDataSourceImpl : FcmTokenDataSource {
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
}

class IosFcmSendNotificationDataSourceImpl : FcmSendNotificationDataSource {
    override suspend fun sendNotification(
        token: String,
        title: String,
        body: String,
    ) {
        // iOS stub — APNs / FCM send not configured yet
    }
}

class IosFcmTokenProviderDataSourceImpl : FcmTokenProviderDataSource {
    override suspend fun getToken(): String = ""
}

class IosNotificationTopicDataSourceImpl : NotificationTopicDataSource {
    override suspend fun subscribe(topic: String) {}

    override suspend fun unsubscribe(topic: String) {}
}

// iOS stub — APNs not configured yet; always returns false
class IosNotificationPermissionDataSourceImpl : NotificationPermissionDataSource {
    override fun isGranted(): Boolean = false
}
