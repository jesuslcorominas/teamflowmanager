package com.jesuslcorominas.teamflowmanager.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.jesuslcorominas.teamflowmanager.domain.notification.MatchNotificationController
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MatchCountdownService : Service() {

    private val matchNotificationController: MatchNotificationController by inject()
    private val timeProvider: TimeProvider by inject()

    private lateinit var notificationManager: MatchNotificationManager
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var updateJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = MatchNotificationManager(this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> startForegroundNotification()
            ACTION_STOP_SERVICE -> stopForegroundService()
        }

        return START_STICKY
    }

    private fun startForegroundNotification() {
        updateJob?.cancel()
        updateJob =
            serviceScope.launch {
                matchNotificationController.getActiveMatch().collect { match ->
                    if (match != null) {
                        val notification =
                            notificationManager.buildNotification(
                                match,
                                timeProvider.getCurrentTime(),
                            )
                        startForeground(MatchNotificationManager.NOTIFICATION_ID, notification)
                    } else {
                        // No active match, stop the service
                        stopForegroundService()
                    }
                }
            }
    }

    private fun stopForegroundService() {
        updateJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
    }

    companion object {
        const val ACTION_START_SERVICE = "com.jesuslcorominas.teamflowmanager.ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.jesuslcorominas.teamflowmanager.ACTION_STOP_SERVICE"
    }
}
