package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationTopicDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationTopicRepository

class NotificationTopicRepositoryImpl(
    private val notificationTopicDataSource: NotificationTopicDataSource,
) : NotificationTopicRepository {
    override suspend fun subscribeToClub(clubId: String) = notificationTopicDataSource.subscribe("club_$clubId")

    override suspend fun unsubscribeFromClub(clubId: String) = notificationTopicDataSource.unsubscribe("club_$clubId")

    override suspend fun unsubscribeFromRawTopic(topic: String) = notificationTopicDataSource.unsubscribe(topic)
}
