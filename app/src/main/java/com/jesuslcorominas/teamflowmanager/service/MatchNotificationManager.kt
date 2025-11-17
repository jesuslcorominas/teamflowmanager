package com.jesuslcorominas.teamflowmanager.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.ui.main.MainActivity

class MatchNotificationManager(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.match_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = context.getString(R.string.match_notification_channel_description)
                    setShowBadge(false)
                }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        match: Match,
        currentTimeMillis: Long,
    ): android.app.Notification {
        val contentIntent = createContentIntent()

        val notificationBuilder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.match_notification_title, match.opponent))
                .setSmallIcon(R.drawable.ic_timer)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)

        // Calculate remaining time
        when (match.status) {
            MatchStatus.IN_PROGRESS -> {
                val currentPeriod =
                    match.periods.firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }
                        ?: match.periods.last()

                val elapsedTime = currentTimeMillis - currentPeriod.startTimeMillis
                val remainingTime = currentPeriod.periodDuration - elapsedTime
                val whenTime = System.currentTimeMillis() + remainingTime

                notificationBuilder
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setWhen(whenTime)
                    .setContentText(context.getString(R.string.match_notification_in_progress))
            }
            MatchStatus.PAUSED -> {
                notificationBuilder
                    .setContentText(context.getString(R.string.match_notification_paused))
            }
            MatchStatus.TIMEOUT -> {
                notificationBuilder
                    .setContentText(context.getString(R.string.match_notification_timeout))
            }
            else -> {
                notificationBuilder
                    .setContentText(context.getString(R.string.match_notification_scheduled))
            }
        }

        // Add action buttons based on match status
        addNotificationActions(notificationBuilder, match)

        return notificationBuilder.build()
    }

    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        match: Match,
    ) {
        when (match.status) {
            MatchStatus.IN_PROGRESS -> {
                // Add pause action
                if (match.canPause()) {
                    val pauseIntent =
                        createActionIntent(
                            ACTION_PAUSE_MATCH,
                            match.id,
                        )
                    builder.addAction(
                        R.drawable.ic_pause,
                        context.getString(R.string.match_notification_action_pause),
                        pauseIntent,
                    )
                }

                // Add timeout action
                val timeoutIntent =
                    createActionIntent(
                        ACTION_START_TIMEOUT,
                        match.id,
                    )
                builder.addAction(
                    R.drawable.ic_timeout,
                    context.getString(R.string.match_notification_action_timeout),
                    timeoutIntent,
                )
            }
            MatchStatus.PAUSED -> {
                // Add resume action
                val resumeIntent =
                    createActionIntent(
                        ACTION_RESUME_MATCH,
                        match.id,
                    )
                builder.addAction(
                    R.drawable.ic_play,
                    context.getString(R.string.match_notification_action_resume),
                    resumeIntent,
                )
            }
            MatchStatus.TIMEOUT -> {
                // Add end timeout action
                val endTimeoutIntent =
                    createActionIntent(
                        ACTION_END_TIMEOUT,
                        match.id,
                    )
                builder.addAction(
                    R.drawable.ic_play,
                    context.getString(R.string.match_notification_action_end_timeout),
                    endTimeoutIntent,
                )
            }
            else -> {
                // No actions for scheduled or finished matches
            }
        }
    }

    private fun createContentIntent(): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createActionIntent(
        action: String,
        matchId: Long,
    ): PendingIntent {
        val intent =
            Intent(context, MatchCountdownService::class.java).apply {
                this.action = action
                putExtra(EXTRA_MATCH_ID, matchId)
            }
        return PendingIntent.getService(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_ID = "match_countdown_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PAUSE_MATCH = "com.jesuslcorominas.teamflowmanager.ACTION_PAUSE_MATCH"
        const val ACTION_RESUME_MATCH = "com.jesuslcorominas.teamflowmanager.ACTION_RESUME_MATCH"
        const val ACTION_START_TIMEOUT = "com.jesuslcorominas.teamflowmanager.ACTION_START_TIMEOUT"
        const val ACTION_END_TIMEOUT = "com.jesuslcorominas.teamflowmanager.ACTION_END_TIMEOUT"
        const val EXTRA_MATCH_ID = "extra_match_id"
    }
}
