package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PresidentNotificationDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository
import kotlinx.coroutines.flow.Flow

internal class PresidentNotificationRepositoryImpl(
    private val dataSource: PresidentNotificationDataSource,
) : PresidentNotificationRepository {
    override fun getNotifications(clubId: String): Flow<List<PresidentNotification>> = dataSource.getNotifications(clubId)

    override fun getUnreadCount(clubId: String): Flow<Int> = dataSource.getUnreadCount(clubId)

    override suspend fun markAsRead(
        clubId: String,
        notificationId: String,
    ) {
        dataSource.markAsRead(clubId, notificationId)
    }

    override suspend fun markAsUnread(
        clubId: String,
        notificationId: String,
    ) {
        dataSource.markAsUnread(clubId, notificationId)
    }

    override suspend fun deleteNotification(
        clubId: String,
        notificationId: String,
    ) {
        dataSource.deleteNotification(clubId, notificationId)
    }
}
