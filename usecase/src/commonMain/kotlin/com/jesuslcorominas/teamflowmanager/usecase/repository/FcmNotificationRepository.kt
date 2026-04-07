package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload

interface FcmNotificationRepository {
    suspend fun sendNotificationToUser(userId: String, payload: NotificationPayload)
}
