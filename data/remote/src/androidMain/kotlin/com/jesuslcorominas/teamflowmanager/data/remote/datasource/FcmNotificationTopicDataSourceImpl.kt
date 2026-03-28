package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.messaging.FirebaseMessaging
import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationTopicDataSource
import kotlinx.coroutines.tasks.await

class FcmNotificationTopicDataSourceImpl : NotificationTopicDataSource {
    override suspend fun subscribe(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
    }

    override suspend fun unsubscribe(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
    }
}
