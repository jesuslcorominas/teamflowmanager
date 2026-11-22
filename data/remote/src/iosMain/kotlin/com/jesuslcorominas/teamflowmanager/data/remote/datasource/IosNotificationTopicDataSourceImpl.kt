package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationTopicDataSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.ios
import dev.gitlive.firebase.messaging.messaging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSError

/**
 * Topic (un)subscription through the native `FIRMessaging`. gitlive's wrapper fires and
 * forgets; the completion-handler variants are used instead so a failure surfaces to the
 * caller, as `subscribeToTopic(...).await()` does on Android.
 */
@OptIn(ExperimentalForeignApi::class)
class IosNotificationTopicDataSourceImpl : NotificationTopicDataSource {
    override suspend fun subscribe(topic: String) = awaitTopicOperation { completion -> Firebase.messaging.ios.subscribeToTopic(topic, completion) }

    override suspend fun unsubscribe(topic: String) = awaitTopicOperation { completion -> Firebase.messaging.ios.unsubscribeFromTopic(topic, completion) }

    private suspend fun awaitTopicOperation(operation: (completion: (NSError?) -> Unit) -> Unit) {
        val result = CompletableDeferred<Unit>()
        operation { error ->
            if (error == null) {
                result.complete(Unit)
            } else {
                result.completeExceptionally(IllegalStateException(error.localizedDescription))
            }
        }
        result.await()
    }
}
