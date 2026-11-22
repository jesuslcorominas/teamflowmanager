package com.jesuslcorominas.teamflowmanager

import platform.Foundation.NSLog
import platform.UIKit.UIApplication
import platform.UIKit.registerForRemoteNotifications
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Asks the user to allow notifications and, when allowed, registers with APNs so
 * `AppDelegate` receives the device token and hands it to Firebase Messaging.
 *
 * iOS shows the system dialog only while the choice is undecided; afterwards the call
 * answers straight away with the stored choice, so it is safe to call on every launch.
 * Apple asks apps to register on every launch too, because the APNs token can change.
 *
 * Swift calls it as `PushPermissionKt.requestPushPermission { granted in ... }`.
 * [onResult] runs on the main thread.
 */
fun requestPushPermission(onResult: (Boolean) -> Unit) {
    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
        UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
    ) { granted, error ->
        if (error != null) {
            NSLog("TFMPush authorization request failed: ${error.localizedDescription}".replace("%", "%%"))
        }
        dispatch_async(dispatch_get_main_queue()) {
            if (granted) UIApplication.sharedApplication.registerForRemoteNotifications()
            onResult(granted)
        }
    }
}
