package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationPermissionDataSource
import platform.Foundation.NSLog
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_wait
import platform.darwin.dispatch_time
import kotlin.concurrent.AtomicInt

/**
 * Whether the user allows this app to show notifications.
 *
 * The contract is synchronous while `UNUserNotificationCenter` only answers through a
 * completion handler, so the call waits for it on a semaphore. The handler runs on a
 * background queue, so waiting on the main thread cannot deadlock; the answer arrives
 * in milliseconds and the timeout only guards against a system that never replies.
 *
 * The permission itself is requested by `AppDelegate` at launch (as `MainActivity`
 * does on Android) and, on login, through `LoginScreen.onRequestNotificationPermission`.
 */
internal class IosNotificationPermissionDataSourceImpl : NotificationPermissionDataSource {
    override fun isGranted(): Boolean {
        val granted = AtomicInt(NOT_ANSWERED)
        val semaphore = dispatch_semaphore_create(0)
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            val allowed =
                status == UNAuthorizationStatusAuthorized ||
                    status == UNAuthorizationStatusProvisional ||
                    status == UNAuthorizationStatusEphemeral
            granted.value = if (allowed) GRANTED else DENIED
            dispatch_semaphore_signal(semaphore)
        }
        dispatch_semaphore_wait(semaphore, dispatch_time(DISPATCH_TIME_NOW, TIMEOUT_NANOS))
        if (granted.value == NOT_ANSWERED) NSLog("TFMPush notification settings did not answer in time")
        return granted.value == GRANTED
    }

    private companion object {
        const val NOT_ANSWERED = 0
        const val GRANTED = 1
        const val DENIED = 2
        const val TIMEOUT_NANOS = 2_000_000_000L
    }
}
