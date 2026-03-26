package com.jesuslcorominas.teamflowmanager.usecase.repository

interface NotificationTopicRepository {
    suspend fun subscribeToClub(clubFirestoreId: String)
    suspend fun unsubscribeFromClub(clubFirestoreId: String)
    suspend fun unsubscribeFromRawTopic(topic: String)
}
