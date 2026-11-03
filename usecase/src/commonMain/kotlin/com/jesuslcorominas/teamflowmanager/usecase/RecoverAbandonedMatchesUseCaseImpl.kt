package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.analytics.AbandonedMatchRecoveredException
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.ClearPendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObservePendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RecoverAbandonedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import kotlinx.coroutines.flow.first
import kotlin.coroutines.cancellation.CancellationException

internal class RecoverAbandonedMatchesUseCaseImpl(
    private val getAllMatches: GetAllMatchesUseCase,
    private val finishMatch: FinishMatchUseCase,
    private val endTimeout: EndTimeoutUseCase,
    private val observePendingSubstitutions: ObservePendingSubstitutionsUseCase,
    private val clearPendingSubstitutions: ClearPendingSubstitutionsUseCase,
    private val timeProvider: TimeProvider,
    private val crashReporter: CrashReporter,
) : RecoverAbandonedMatchesUseCase {
    override suspend fun invoke(): Int {
        val now = timeProvider.getCurrentTime()
        val abandoned =
            try {
                getAllMatches().first().mapNotNull { match ->
                    match.inactivityAt(now)?.let { match to it }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                crashReporter.log("RecoverAbandonedMatches: could not read the matches: ${e.message}")
                crashReporter.recordException(e)
                return 0
            }

        var recovered = 0
        abandoned.forEach { (match, inactivityMillis) ->
            // One bad match must not strand the rest: each is reported and the sweep carries on.
            // Cancellation is not a failure, though — the sweep runs detached from the splash, so
            // the scope dying is routine, and reporting it would bury the real recoveries this
            // whole use case exists to surface.
            try {
                recover(match, inactivityMillis, now)
                recovered++
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                crashReporter.log("RecoverAbandonedMatches: failed to recover match ${match.id}: ${e.message}")
                crashReporter.recordException(e)
            }
        }
        return recovered
    }

    private suspend fun recover(
        match: Match,
        inactivityMillis: Long,
        now: Long,
    ) {
        val foundAs = match.status
        val pendingCount = match.pendingSubstitutionCount()

        if (foundAs == MatchStatus.TIMEOUT) {
            // Close the timeout at the very instant it began, so it adds nothing to the running
            // period: nobody knows when the coach would have ended it. FinishMatchUseCase only
            // accepts IN_PROGRESS and PAUSED, so this is also what makes the finish below reachable.
            endTimeout(match.id, match.timeoutStartTimeMillis)
        }

        finishMatch(match.id, match.finishTimeCappedTo(now))

        // Only once the finish went through: a match left running with its queue already emptied
        // would be worse than the orphan, and the next cold start would retry this one anyway.
        clearPendingSubstitutions(match.id)

        report(match, foundAs, inactivityMillis, pendingCount)
    }

    /**
     * Reads the queue before it is emptied — an orphan that still had scheduled changes is exactly
     * the case that makes the odd states of the substitution-mode setting reachable, so it is worth
     * knowing. Never lets a failure here cost us the recovery itself.
     */
    private suspend fun Match.pendingSubstitutionCount(): Int =
        try {
            observePendingSubstitutions(id).first().size
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            crashReporter.log("RecoverAbandonedMatches: could not read the pending queue of $id: ${e.message}")
            0
        }

    private fun report(
        match: Match,
        foundAs: MatchStatus,
        inactivityMillis: Long,
        pendingCount: Int,
    ) {
        val inactivityMinutes = (inactivityMillis / MILLIS_PER_MINUTE).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        crashReporter.setCustomKey(KEY_MATCH_ID, match.id)
        crashReporter.setCustomKey(KEY_STATUS, foundAs.name)
        crashReporter.setCustomKey(KEY_INACTIVITY_MINUTES, inactivityMinutes)
        crashReporter.setCustomKey(KEY_HAD_PENDING, pendingCount > 0)
        crashReporter.setCustomKey(KEY_PENDING_COUNT, pendingCount)

        val message =
            "Abandoned match auto-finished: matchId=${match.id}, foundAs=${foundAs.name}, " +
                "inactiveForMinutes=$inactivityMinutes, pendingSubstitutions=$pendingCount"
        crashReporter.log(message)
        crashReporter.recordException(AbandonedMatchRecoveredException(message))
    }

    /**
     * How long this match has gone without activity, or null when it is not one we should recover.
     *
     * Inactivity that is zero or negative — a device clock moved backwards — never qualifies, which
     * the threshold being positive already guarantees. Everything here errs towards leaving the
     * match alone: finishing a live one is worse than leaving an orphan a few hours longer.
     */
    private fun Match.inactivityAt(now: Long): Long? {
        if (archived) return null
        val lastActivity = lastActivityAt() ?: return null
        return (now - lastActivity).takeIf { it >= ABANDONED_AFTER_MILLIS }
    }

    /**
     * The last moment we know the coach was doing something with this match, derived from
     * timestamps the model already carries — no extra field and no extra query.
     *
     * Null for any match that is not running, and also for a running one whose timestamps are all
     * empty: that is corrupt state we would rather report than auto-finish on a guess.
     */
    private fun Match.lastActivityAt(): Long? {
        val fromStatus =
            when (status) {
                MatchStatus.IN_PROGRESS -> activePeriod()?.startTimeMillis
                MatchStatus.PAUSED -> periods.lastOrNull { it.endTimeMillis > 0L }?.endTimeMillis
                MatchStatus.TIMEOUT -> timeoutStartTimeMillis
                else -> return null
            }
        return fromStatus?.takeIf { it > 0L } ?: dateTime?.takeIf { it > 0L }
    }

    /**
     * The instant to finish at, never the clock of right now.
     *
     * `FinishMatchUseCase` uses what it is given to close the running period and to settle the
     * elapsed time of every player whose timer was still going. Handed the real current time, a
     * 25-minute match recovered six hours later would record six hours of play for each starter.
     *
     * A paused match needs no guesswork: its period is already closed and no player timer is
     * running, so the instant of the pause reproduces the stored times exactly. A running one is
     * capped at the nominal end of its period — still an approximation, since nobody knows the
     * minute the process died, but bounded by something physically possible.
     */
    private fun Match.finishTimeCappedTo(now: Long): Long =
        when (status) {
            MatchStatus.PAUSED -> periods.lastOrNull { it.endTimeMillis > 0L }?.endTimeMillis ?: now
            else -> activePeriod()?.let { minOf(now, it.startTimeMillis + it.periodDuration) } ?: now
        }

    private fun Match.activePeriod(): MatchPeriod? = periods.lastOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }

    companion object {
        private const val MILLIS_PER_MINUTE = 60 * 1000L

        /**
         * How long a match has to sit untouched before we treat it as abandoned.
         *
         * Six hours is roughly eight times the longest break that could still be a real match: a
         * half-time break runs ten to fifteen minutes, forty-five in a pathological one with a
         * delayed restart, and a whole match with warm-up and extra time does not reach two and a
         * half hours. Waiting costs nothing — an orphan only gets in the way when the coach opens
         * Settings, and a coach doing that within six hours of kick-off is probably still at the
         * match — while firing early would finish a game that is actually being played.
         */
        internal const val ABANDONED_AFTER_MILLIS = 6 * 60 * 60 * 1000L

        private const val KEY_MATCH_ID = "abandoned_match_id"
        private const val KEY_STATUS = "abandoned_match_status"
        private const val KEY_INACTIVITY_MINUTES = "abandoned_inactivity_minutes"
        private const val KEY_HAD_PENDING = "abandoned_had_pending_substitutions"
        private const val KEY_PENDING_COUNT = "abandoned_pending_substitutions_count"
    }
}
