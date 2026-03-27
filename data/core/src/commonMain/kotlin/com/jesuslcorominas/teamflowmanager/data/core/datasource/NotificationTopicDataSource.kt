package com.jesuslcorominas.teamflowmanager.data.core.datasource

interface NotificationTopicDataSource {
    suspend fun subscribe(topic: String)

    suspend fun unsubscribe(topic: String)
}
