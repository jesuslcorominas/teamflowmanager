package com.jesuslcorominas.teamflowmanager.usecase.repository

interface NotificationTopicRepository {
    suspend fun subscribeToClub(clubId: String)

    suspend fun unsubscribeFromClub(clubId: String)

    suspend fun unsubscribeFromRawTopic(topic: String)
}
