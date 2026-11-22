package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenProviderDataSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.messaging
import kotlinx.coroutines.CancellationException
import platform.Foundation.NSLog

/**
 * FCM registration token of this device. Firebase can only issue it once the APNs
 * token has been handed to `Messaging` (see `AppDelegate` in iOSApp.swift), so it is
 * empty until the app has registered for remote notifications.
 *
 * Like the Android implementation, a failure yields an empty token: the sync use case
 * skips empty tokens and retries on the next launch or login.
 */
internal class IosFcmTokenProviderDataSourceImpl : FcmTokenProviderDataSource {
    override suspend fun getToken(): String =
        try {
            Firebase.messaging.getToken()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NSLog("TFMPush FCM token unavailable: ${e.message}".replace("%", "%%"))
            ""
        }
}
