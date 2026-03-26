package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenProviderDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationTopicDataSource

// iOS stubs — replace with real implementations when APNs is configured in Firebase Console

class IosFcmTokenDataSourceImpl : FcmTokenDataSource {
    override suspend fun saveToken(userId: String, token: String, platform: String) {}
    override suspend fun deleteToken(userId: String, token: String) {}
}

class IosFcmTokenProviderDataSourceImpl : FcmTokenProviderDataSource {
    override suspend fun getToken(): String = ""
}

class IosNotificationTopicDataSourceImpl : NotificationTopicDataSource {
    override suspend fun subscribe(topic: String) {}
    override suspend fun unsubscribe(topic: String) {}
}
