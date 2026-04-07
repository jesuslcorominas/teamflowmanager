package com.jesuslcorominas.teamflowmanager.data.core.datasource

/**
 * DataSource for sending FCM push notifications to specific device tokens.
 * Implementations call a server-side endpoint (e.g., Firebase Cloud Function)
 * that uses the Firebase Admin SDK to deliver notifications.
 */
interface FcmSendNotificationDataSource {
    /**
     * Sends a push notification to the given FCM token.
     *
     * @param token The FCM device registration token of the recipient
     * @param title The notification title
     * @param body The notification body text
     */
    suspend fun sendNotification(
        token: String,
        title: String,
        body: String,
    )
}
