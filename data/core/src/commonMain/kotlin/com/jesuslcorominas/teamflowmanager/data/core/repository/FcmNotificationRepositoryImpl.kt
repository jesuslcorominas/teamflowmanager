package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmNotificationRepository

private const val NOTIFICATION_TITLE = "Has sido asignado como entrenador"

class FcmNotificationRepositoryImpl(
    private val fcmTokenDataSource: FcmTokenDataSource,
) : FcmNotificationRepository {
    override suspend fun sendNotificationToUser(
        userId: String,
        title: String,
        body: String,
    ) {
        val tokens = fcmTokenDataSource.getTokensByUserId(userId)
        tokens.forEach { token ->
            fcmTokenDataSource.sendNotification(token = token, title = title, body = body)
        }
    }
}
