package com.jesuslcorominas.teamflowmanager.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType

class MatchNotificationManager(private val context: Context) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
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

    fun buildNotification(
        match: Match,
        currentTimeMillis: Long,
    ): android.app.Notification {
        val contentIntent = createContentIntent(match.id)

        // Show score in title
        val title =
            context.getString(
                R.string.match_score,
                match.goals,
                match.opponentGoals,
            ) + " - " + match.opponent

        val periodName = getPeriodName(match)

        val notificationBuilder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher_black_and_white)
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

                notificationBuilder
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setWhen(whenTime)
                    .setContentText(periodName)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(periodName)
                            .setBigContentTitle(title),
                    )
            }
            MatchStatus.PAUSED -> {
                notificationBuilder
                    .setContentText(periodName)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(periodName)
                            .setBigContentTitle(title),
                    )
            }
            MatchStatus.TIMEOUT -> {
                notificationBuilder
                    .setContentText(context.getString(R.string.match_timeout))
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(context.getString(R.string.match_timeout))
                            .setBigContentTitle(title),
                    )
            }
            else -> {
                notificationBuilder
                    .setContentText(context.getString(R.string.match_next))
            }
        }

        return notificationBuilder.build()
    }

    private fun getPeriodName(match: Match): String {
        val matchStatus = match.status
        val numberOfPauses = match.pauseCount

        val currentPeriod =
            match
                .periods
                .firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }
                ?: match.periods.last()

        return when {
            matchStatus == MatchStatus.TIMEOUT -> context.getString(R.string.match_timeout)
            matchStatus == MatchStatus.PAUSED &&
                (match.periodType == PeriodType.HALF_TIME || numberOfPauses == 2) ->
                context.getString(R.string.paused_match_half_time)

            matchStatus == MatchStatus.PAUSED &&
                match.periodType == PeriodType.QUARTER_TIME &&
                (numberOfPauses == 1 || numberOfPauses == 3) ->
                context.getString(R.string.paused_match_quarter_break)

            match.periodType == PeriodType.HALF_TIME &&
                currentPeriod.periodNumber == 1 -> context.getString(R.string.first_half)

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

    private fun createContentIntent(matchId: Long): PendingIntent {
        val deepLinkUri = Uri.parse("teamflowmanager://match/$matchId")
        val intent =
            Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        return PendingIntent.getActivity(
            context,
            matchId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_ID = "match_countdown_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_OPEN_MATCH = "com.jesuslcorominas.teamflowmanager.ACTION_OPEN_MATCH"
        const val EXTRA_MATCH_ID = "extra_match_id"
    }
}
