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
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
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
        val contentIntent = createContentIntent(match.id)

        // Show score in title
        val title = context.getString(
            R.string.match_score,
            match.goals,
            match.opponentGoals
        ) + " - " + match.opponent

        val periodName = getPeriodName(match)

        val notificationBuilder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher) // Use app icon
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)

        // Calculate remaining time and set content text
        when (match.status) {
            MatchStatus.IN_PROGRESS -> {
                val currentPeriod =
                    match.periods.firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }
                        ?: match.periods.last()

                val elapsedTime = currentTimeMillis - currentPeriod.startTimeMillis
                val remainingTime = currentPeriod.periodDuration - elapsedTime
                val whenTime = System.currentTimeMillis() + remainingTime

                // Format remaining time for display
                val minutes = (remainingTime / 60000).toInt()
                val seconds = ((remainingTime % 60000) / 1000).toInt()
                val timeText = String.format("%02d:%02d", minutes, seconds)

                notificationBuilder
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setWhen(whenTime)
                    .setContentText("$periodName • $timeText")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("$periodName\n\n$timeText")
                            .setBigContentTitle(title)
                    )
            }
            MatchStatus.PAUSED -> {
                notificationBuilder
                    .setContentText(periodName)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(periodName)
                            .setBigContentTitle(title)
                    )
            }
            MatchStatus.TIMEOUT -> {
                notificationBuilder
                    .setContentText(context.getString(R.string.match_timeout))
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(context.getString(R.string.match_timeout))
                            .setBigContentTitle(title)
                    )
            }
            else -> {
                notificationBuilder
                    .setContentText(context.getString(R.string.match_next))
            }
        }

        // Add action buttons based on match status
        addNotificationActions(notificationBuilder, match)

        return notificationBuilder.build()
    }

    private fun getPeriodName(match: Match): String {
        val matchStatus = match.status
        val numberOfPauses = match.pauseCount

        val currentPeriod = match
            .periods
            .firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }
            ?: match.periods.last()

        return when {
            matchStatus == MatchStatus.TIMEOUT -> context.getString(R.string.match_timeout)
            matchStatus == MatchStatus.PAUSED
                && (match.periodType == PeriodType.HALF_TIME || numberOfPauses == 2) ->
                context.getString(R.string.paused_match_half_time)

            matchStatus == MatchStatus.PAUSED
                && match.periodType == PeriodType.QUARTER_TIME
                && (numberOfPauses == 1 || numberOfPauses == 3) ->
                context.getString(R.string.paused_match_quarter_break)

            match.periodType == PeriodType.HALF_TIME
                && currentPeriod.periodNumber == 1 -> context.getString(R.string.first_half)

            match.periodType == PeriodType.HALF_TIME && currentPeriod.periodNumber == 2 ->
                context.getString(R.string.second_half)

            match.periodType == PeriodType.QUARTER_TIME && currentPeriod.periodNumber == 1 ->
                context.getString(R.string.first_quarter)

            match.periodType == PeriodType.QUARTER_TIME && currentPeriod.periodNumber == 2 ->
                context.getString(R.string.second_quarter)

            match.periodType == PeriodType.QUARTER_TIME && currentPeriod.periodNumber == 3 ->
                context.getString(R.string.third_quarter)

            match.periodType == PeriodType.QUARTER_TIME && currentPeriod.periodNumber == 4 ->
                context.getString(R.string.fourth_quarter)

            else ->
                context.getString(R.string.period_label, currentPeriod.periodNumber, match.periodType.numberOfPeriods)
        }
    }

    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        match: Match,
    ) {
        when (match.status) {
            MatchStatus.IN_PROGRESS -> {
                // Order: Gol - Tiempo Muerto - Pausa - Fin partido - Gol rival
                // Android typically allows max 3 actions, so prioritize: Tiempo Muerto - Pausa - Fin partido
                
                // Add timeout action
                val timeoutIntent =
                    createActionIntent(
                        ACTION_START_TIMEOUT,
                        match.id,
                    )
                builder.addAction(
                    R.drawable.ic_timeout,
                    context.getString(R.string.timeout_button),
                    timeoutIntent,
                )

                // Add pause action if allowed
                if (match.canPause()) {
                    val pauseIntent =
                        createActionIntent(
                            ACTION_PAUSE_MATCH,
                            match.id,
                        )
                    builder.addAction(
                        R.drawable.ic_pause,
                        context.getString(R.string.pause_match_button),
                        pauseIntent,
                    )
                }

                // Add finish match action - only during IN_PROGRESS
                val finishIntent = createFinishMatchIntent(match.id)
                builder.addAction(
                    R.drawable.ic_whistle,
                    context.getString(R.string.finish_match_button),
                    finishIntent,
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
                    context.getString(R.string.resume_match_button),
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
                    context.getString(R.string.finish_timeout_button),
                    endTimeoutIntent,
                )
            }
            else -> {
                // No actions for scheduled or finished matches
            }
        }
    }

    private fun createContentIntent(matchId: Long): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_MATCH_ID, matchId)
                action = ACTION_OPEN_MATCH
            }
        return PendingIntent.getActivity(
            context,
            matchId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createFinishMatchIntent(matchId: Long): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_MATCH_ID, matchId)
                action = ACTION_FINISH_MATCH
            }
        return PendingIntent.getActivity(
            context,
            matchId.toInt() + 30000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createGoalIntent(matchId: Long, isHomeGoal: Boolean): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_MATCH_ID, matchId)
                action = if (isHomeGoal) ACTION_ADD_HOME_GOAL else ACTION_ADD_VISITOR_GOAL
            }
        return PendingIntent.getActivity(
            context,
            if (isHomeGoal) matchId.toInt() + 10000 else matchId.toInt() + 20000,
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
        const val ACTION_OPEN_MATCH = "com.jesuslcorominas.teamflowmanager.ACTION_OPEN_MATCH"
        const val ACTION_FINISH_MATCH = "com.jesuslcorominas.teamflowmanager.ACTION_FINISH_MATCH"
        const val ACTION_ADD_HOME_GOAL = "com.jesuslcorominas.teamflowmanager.ACTION_ADD_HOME_GOAL"
        const val ACTION_ADD_VISITOR_GOAL = "com.jesuslcorominas.teamflowmanager.ACTION_ADD_VISITOR_GOAL"
        const val EXTRA_MATCH_ID = "extra_match_id"
    }
}
