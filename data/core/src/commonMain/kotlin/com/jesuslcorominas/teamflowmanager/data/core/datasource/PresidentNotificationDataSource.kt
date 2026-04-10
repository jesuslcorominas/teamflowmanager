package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import kotlinx.coroutines.flow.Flow

interface PresidentNotificationDataSource {
    fun getNotifications(clubId: String): Flow<List<PresidentNotification>>

    fun getUnreadCount(clubId: String): Flow<Int>

    suspend fun createNotification(
        clubId: String,
        notification: PresidentNotification,
    )

    suspend fun markAsRead(
        clubId: String,
        notificationId: String,
    )

    suspend fun markAsUnread(
        clubId: String,
        notificationId: String,
    )

    suspend fun deleteNotification(
        clubId: String,
        notificationId: String,
    )
}
