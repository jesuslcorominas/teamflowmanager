package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationTopicDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationTopicRepository

class NotificationTopicRepositoryImpl(
    private val notificationTopicDataSource: NotificationTopicDataSource,
) : NotificationTopicRepository {
    override suspend fun subscribeToClub(clubFirestoreId: String) =
        notificationTopicDataSource.subscribe("club_$clubFirestoreId")

    override suspend fun unsubscribeFromClub(clubFirestoreId: String) =
        notificationTopicDataSource.unsubscribe("club_$clubFirestoreId")
}
