package com.jesuslcorominas.teamflowmanager.domain.usecase

/**
 * Finishes the matches that were left open and then forgotten.
 *
 * A match only leaves [com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus.IN_PROGRESS],
 * `PAUSED` or `TIMEOUT` through an explicit finish, so a process killed mid-match — force stop, a
 * flat battery, the OS reclaiming memory — leaves it running forever. An orphan like that locks the
 * substitution mode setting and keeps its queue of scheduled changes alive on the device.
 *
 * Run on cold start: the orphan exists precisely because the process died, so there is no timer
 * that could have fired meanwhile. What separates an orphan from a match that is merely paused at
 * half time is how long it has gone without activity.
 *
 * @return how many matches were recovered.
 */
interface RecoverAbandonedMatchesUseCase {
    suspend operator fun invoke(): Int
}
