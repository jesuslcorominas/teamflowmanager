package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.MatchEventNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentMatchEventUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Encapsulates notification-related logic for match events, keeping MatchViewModel focused
 * on UI state management. This is a pure Kotlin class with no ViewModel dependency.
 */
internal class MatchNotificationCoordinator(
    private val notifyPresidentMatchEvent: NotifyPresidentMatchEventUseCase,
) {
    /**
     * Calculates the minute-of-play string for the given match state and current clock time.
     * Returns null if no active period is found.
     */
    fun minuteOfPlay(
        match: Match,
        currentTimeMs: Long,
    ): String? {
        val currentPeriod =
            match.periods.firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }
                ?: return null
        val periodDurationMs = currentPeriod.periodDuration
        val periodDurationMin = (periodDurationMs / 60_000L).toInt()
        val completedMin = (currentPeriod.periodNumber - 1) * periodDurationMin
        val elapsedInPeriodMs = (currentTimeMs - currentPeriod.startTimeMillis).coerceAtLeast(0L)
        val elapsedInPeriodMin = (elapsedInPeriodMs / 60_000L).toInt()
        return if (elapsedInPeriodMs <= periodDurationMs) {
            (completedMin + elapsedInPeriodMin).toString()
        } else {
            val extraMin = elapsedInPeriodMin - periodDurationMin
            "${completedMin + periodDurationMin}+$extraMin"
        }
    }

    /**
     * Fires a match event notification using the provided team/match context.
     * Silently no-ops if any required ID is missing or if [buildEvent] returns null.
     *
     * @param scope The coroutine scope to launch the notification in (typically viewModelScope).
     * @param team The currently cached team, or null if not yet loaded.
     * @param matchId The local match ID as a string.
     * @param buildEvent Suspending lambda that produces the [MatchEventNotification] to send.
     */
    fun fireNotification(
        scope: CoroutineScope,
        team: Team?,
        matchId: String,
        buildEvent: suspend () -> MatchEventNotification?,
    ) {
        scope.launch {
            runCatching {
                val teamRemoteId = team?.remoteId ?: return@runCatching
                val clubRemoteId = team.clubRemoteId ?: return@runCatching
                val event = buildEvent() ?: return@runCatching
                notifyPresidentMatchEvent(event, matchId, teamRemoteId, clubRemoteId)
            }
        }
    }
}
