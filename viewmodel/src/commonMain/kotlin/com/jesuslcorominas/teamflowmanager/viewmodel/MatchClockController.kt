package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartPlayerTimersBatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import kotlinx.coroutines.flow.first

/**
 * The mechanics of a match's life: starting it, pausing, resuming, finishing, and the timeouts in
 * between — each with the clock synchronisation, analytics and logging it needs.
 *
 * It decides nothing about the screen. Whether a confirmation dialog is shown is the ViewModel's
 * call; this class only answers [needsEndPeriodConfirmation]. Nor does it own any ordering: that
 * resuming is followed by running the scheduled substitutions is orchestration, and it stays in
 * the ViewModel where it can be read in one place.
 */
internal class MatchClockController(
    private val startMatchTimerUseCase: StartMatchTimerUseCase,
    private val startPlayerTimersBatchUseCase: StartPlayerTimersBatchUseCase,
    private val synchronizeTimeUseCase: SynchronizeTimeUseCase,
    private val pauseMatchUseCase: PauseMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase,
    private val finishMatchUseCase: FinishMatchUseCase,
    private val startTimeoutUseCase: StartTimeoutUseCase,
    private val endTimeoutUseCase: EndTimeoutUseCase,
    private val getMatchById: GetMatchByIdUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) {
    /**
     * Starts the match clock and the starting line-up's timers. Returns the match that was
     * started, so the caller can announce it, or null when there was none.
     */
    suspend fun begin(
        matchId: String,
        currentTimeMillis: Long,
    ): Match? {
        syncTimeBestEffort("start")
        val match = getMatchById(matchId).first() ?: return null
        startMatchTimerUseCase(matchId = match.id, currentTimeMillis)
        // Start all player timers at once using batch operation
        if (match.startingLineupIds.isNotEmpty()) {
            startPlayerTimersBatchUseCase(match.id, match.startingLineupIds, currentTimeMillis)
        }
        return match
    }

    suspend fun pause(
        matchId: String,
        currentTimeMillis: Long,
    ) {
        crashReporter.log("Pausing match: $matchId")
        pauseMatchUseCase(matchId, currentTimeMillis)
        analyticsTracker.logEvent(
            AnalyticsEvent.MATCH_PAUSED,
            mapOf(
                AnalyticsParam.MATCH_ID to matchId,
                AnalyticsParam.DURATION_MINUTES to (currentTimeMillis / 60000).toString(),
            ),
        )
    }

    /**
     * Brings the match back from the break. Suspends until it is running again AND the paused
     * players are back to PLAYING, both under ResumeMatchUseCase's own operation id — which is
     * what lets the caller run the scheduled substitutions straight afterwards.
     *
     * Returns false when there is no such match, so the caller knows nothing happened.
     */
    suspend fun resume(
        matchId: String,
        currentTimeMillis: Long,
    ): Boolean {
        crashReporter.log("Resuming match: $matchId")
        syncTimeBestEffort("resume")
        val match = getMatchById(matchId).first() ?: return false
        resumeMatchUseCase(match.id, currentTimeMillis)
        analyticsTracker.logEvent(
            AnalyticsEvent.MATCH_RESUMED,
            mapOf(AnalyticsParam.MATCH_ID to matchId),
        )
        return true
    }

    suspend fun finish(
        matchId: String,
        currentTimeMillis: Long,
    ) {
        crashReporter.log("Finishing match: $matchId")
        finishMatchUseCase(matchId, currentTimeMillis)
        analyticsTracker.logEvent(
            AnalyticsEvent.MATCH_FINISHED,
            mapOf(
                AnalyticsParam.MATCH_ID to matchId,
                AnalyticsParam.DURATION_MINUTES to (currentTimeMillis / 60000).toString(),
            ),
        )
    }

    suspend fun startTimeout(
        matchId: String,
        currentTimeMillis: Long,
    ) {
        crashReporter.log("Starting timeout for match: $matchId")
        startTimeoutUseCase(matchId, currentTimeMillis)
        analyticsTracker.logEvent(
            AnalyticsEvent.BUTTON_CLICKED,
            mapOf(
                AnalyticsParam.BUTTON_NAME to "start_timeout",
                AnalyticsParam.MATCH_ID to matchId,
            ),
        )
    }

    suspend fun endTimeout(
        matchId: String,
        currentTimeMillis: Long,
    ) {
        crashReporter.log("Ending timeout for match: $matchId")
        endTimeoutUseCase(matchId, currentTimeMillis)
        analyticsTracker.logEvent(
            AnalyticsEvent.BUTTON_CLICKED,
            mapOf(
                AnalyticsParam.BUTTON_NAME to "end_timeout",
                AnalyticsParam.MATCH_ID to matchId,
            ),
        )
    }

    /**
     * Whether ending the period now deserves an "are you sure": more than a minute of normal time
     * still to play. In additional time it does not, and neither does a match with no period
     * running. Pausing and stopping asked this same question in two places; it is one now.
     */
    fun needsEndPeriodConfirmation(
        match: Match,
        currentTimeMillis: Long,
    ): Boolean {
        // Calculate remaining time in current period
        val currentPeriod =
            match.periods
                .firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }
                ?: return false
        val elapsedTime = (currentTimeMillis - currentPeriod.startTimeMillis).coerceAtLeast(0L)
        val remainingTime = currentPeriod.periodDuration - elapsedTime
        // If more than 1 minute remains in normal time, show confirmation dialog
        // If in additional time (remainingTime <= 0), proceed without confirmation
        return remainingTime > 60000L
    }

    /** The clock is nice to have in sync, but never a reason not to start or resume. */
    private suspend fun syncTimeBestEffort(context: String) {
        try {
            synchronizeTimeUseCase()
        } catch (e: Exception) {
            crashReporter.recordException(e)
            crashReporter.log("Error synchronizing time before match $context: ${e.message}")
            // Continue even if sync fails
        }
    }
}
