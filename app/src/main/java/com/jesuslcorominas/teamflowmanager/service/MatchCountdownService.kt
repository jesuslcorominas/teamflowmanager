package com.jesuslcorominas.teamflowmanager.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.jesuslcorominas.teamflowmanager.domain.notification.MatchNotificationController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MatchCountdownService : Service() {

    private val matchNotificationController: MatchNotificationController by inject()

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
            ACTION_START_SERVICE -> {
                startForegroundNotification()
            }
            ACTION_STOP_SERVICE -> {
                stopForegroundService()
            }
            MatchNotificationManager.ACTION_PAUSE_MATCH -> {
                handlePauseMatch(intent)
            }
            MatchNotificationManager.ACTION_RESUME_MATCH -> {
                handleResumeMatch(intent)
            }
            MatchNotificationManager.ACTION_START_TIMEOUT -> {
                handleStartTimeout(intent)
            }
            MatchNotificationManager.ACTION_END_TIMEOUT -> {
                handleEndTimeout(intent)
            }
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
                                System.currentTimeMillis(),
                            )
                        startForeground(MatchNotificationManager.NOTIFICATION_ID, notification)
                    } else {
                        // No active match, stop the service
                        stopForegroundService()
                    }
                }
            }
    }

    private fun handlePauseMatch(intent: Intent) {
        val matchId = intent.getLongExtra(MatchNotificationManager.EXTRA_MATCH_ID, -1L)
        if (matchId != -1L) {
            serviceScope.launch {
                matchNotificationController.pauseMatch(matchId, System.currentTimeMillis())
                updateNotification()
            }
        }
    }

    private fun handleResumeMatch(intent: Intent) {
        val matchId = intent.getLongExtra(MatchNotificationManager.EXTRA_MATCH_ID, -1L)
        if (matchId != -1L) {
            serviceScope.launch {
                matchNotificationController.resumeMatch(matchId, System.currentTimeMillis())
                updateNotification()
            }
        }
    }

    private fun handleStartTimeout(intent: Intent) {
        val matchId = intent.getLongExtra(MatchNotificationManager.EXTRA_MATCH_ID, -1L)
        if (matchId != -1L) {
            serviceScope.launch {
                matchNotificationController.startTimeout(matchId, System.currentTimeMillis())
                updateNotification()
            }
        }
    }

    private fun handleEndTimeout(intent: Intent) {
        val matchId = intent.getLongExtra(MatchNotificationManager.EXTRA_MATCH_ID, -1L)
        if (matchId != -1L) {
            serviceScope.launch {
                matchNotificationController.endTimeout(matchId, System.currentTimeMillis())
                updateNotification()
            }
        }
    }

    private suspend fun updateNotification() {
        val match = matchNotificationController.getActiveMatch().firstOrNull()
        if (match != null) {
            val notification =
                notificationManager.buildNotification(
                    match,
                    System.currentTimeMillis(),
                )
            startForeground(MatchNotificationManager.NOTIFICATION_ID, notification)
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
