package com.jesuslcorominas.teamflowmanager.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.TeamFlowManagerApplication
import java.util.concurrent.atomic.AtomicInteger

class TeamFlowFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // Token refresh — SyncFcmTokenUseCase is called on next login.
        // No userId is available here so we skip Firestore write until then.
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: return
        val notifId =
            if (message.notification?.tag != null) {
                MATCH_EVENT_NOTIFICATION_ID
            } else {
                notificationIdCounter.getAndIncrement()
            }
        showNotification(title, body, notifId)
    }

    private fun showNotification(
        title: String,
        body: String,
        notifId: Int = notificationIdCounter.getAndIncrement(),
    ) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent =
            packageManager
                .getLaunchIntentForPackage(packageName)
                ?.let { intent ->
                    PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                }

        val notification =
            NotificationCompat.Builder(this, TeamFlowManagerApplication.PUSH_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_black_and_white)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .apply { if (pendingIntent != null) setContentIntent(pendingIntent) }
                .build()

        notificationManager.notify(notifId, notification)
    }

    companion object {
        private const val MATCH_EVENT_NOTIFICATION_ID = 3000
        private val notificationIdCounter = AtomicInteger(3100)
    }
}
