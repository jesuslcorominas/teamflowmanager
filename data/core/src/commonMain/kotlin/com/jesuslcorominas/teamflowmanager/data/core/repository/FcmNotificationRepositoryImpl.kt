package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmNotificationRepository

class FcmNotificationRepositoryImpl(
    private val fcmDataSource: FcmDataSource,
) : FcmNotificationRepository {
    override suspend fun sendNotificationToUser(
        userId: String,
        title: String,
        body: String,
    ) {
        val tokens = fcmDataSource.getTokensByUserId(userId)
        tokens.forEach { token ->
            fcmDataSource.sendNotification(token = token, title = title, body = body)
        }
    }
}
