package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmSendNotificationDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.api.FcmNotificationApi
import com.jesuslcorominas.teamflowmanager.data.remote.api.model.SendNotificationRequest

/**
 * Android implementation of [FcmSendNotificationDataSource].
 * Delegates to a Firebase Cloud Function that uses the Admin SDK to send FCM messages.
 */
internal class FcmSendNotificationDataSourceImpl(
    private val fcmNotificationApi: FcmNotificationApi,
) : FcmSendNotificationDataSource {
    override suspend fun sendNotification(
        token: String,
        title: String,
        body: String,
    ) {
        fcmNotificationApi.sendNotification(
            SendNotificationRequest(
                token = token,
                title = title,
                body = body,
            ),
        )
    }
}
