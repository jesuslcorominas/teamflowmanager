package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.DiscardedSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerActivityInterval
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent

data class PlayerTimeItem(
    val player: Player,
    val timeMillis: Long,
    val isRunning: Boolean,
    val isPaused: Boolean,
    val substitutionCount: Int = 0,
    val isCaptain: Boolean = false,
)

sealed class MatchUiState {
    data object Loading : MatchUiState()

    data object NoMatch : MatchUiState()

    data class Success(
        val match: Match,
        val currentTime: Long,
        val playerTimes: List<PlayerTimeItem>,
        val timelineEvents: List<TimelineEvent> = emptyList(),
    ) : MatchUiState()

    data class Finished(
        val match: Match,
        val currentTime: Long,
        val playerTimes: List<PlayerTimeItem>,
        val substitutions: List<SubstitutionItem>,
        val timelineEvents: List<TimelineEvent> = emptyList(),
        val scoreEvolution: List<ScorePoint> = emptyList(),
        val playerActivity: List<PlayerActivityInterval> = emptyList(),
    ) : MatchUiState()
}

data class SubstitutionItem(
    val playerOut: Player,
    val playerIn: Player,
    val matchElapsedTimeMillis: Long,
)

/**
 * A scheduled substitution already resolved to players, so the screen can paint a card without
 * looking anything up: [SubstitutionPair] carries ids, and a card needs names and numbers.
 */
data class PendingSubstitutionItem(
    val pair: SubstitutionPair,
    val playerOut: Player,
    val playerIn: Player,
)

/**
 * Scheduling [requested] would drop [displaced], because they share a player. Raised before the
 * destructive write so the coach can confirm or back out; confirming applies it and discards
 * [displaced], dismissing schedules nothing.
 */
data class PendingSubstitutionConflict(
    val requested: PendingSubstitutionItem,
    val displaced: List<PendingSubstitutionItem>,
)

/**
 * Who ran a batch of substitutions. It changes how the outcome is worded and, more importantly,
 * what happens to the cards that were discarded — see [SubstitutionExecutionResult].
 */
enum class SubstitutionExecutionTrigger {
    /** The coach tapped a card's play button, or "substitute all". */
    MANUAL,

    /** The match was resumed after a break and the queue ran on its own, with nobody watching. */
    RESUME,
}

/**
 * Outcome of running a batch, individual or not. [applied] pairs are always unscheduled;
 * [discarded] ones survive a [SubstitutionExecutionTrigger.MANUAL] run — the coach is watching and
 * can fix or delete them — but not a [SubstitutionExecutionTrigger.RESUME] one, where a surviving
 * card would fire again by itself at the next break, silently.
 */
data class SubstitutionExecutionResult(
    val trigger: SubstitutionExecutionTrigger,
    val applied: List<SubstitutionPair>,
    val discarded: List<DiscardedSubstitution>,
)

data class EndPeriodState(
    val isBreak: Boolean,
)

internal fun List<Player>.toPlayerItems(
    playerTimes: List<PlayerTime>,
    currentTime: Long,
    captainId: String,
): List<PlayerTimeItem> =
    this.map { player ->
        val playerTime = playerTimes.find { it.playerId == player.id }
        val displayTime =
            if (playerTime != null) {
                calculatePlayerCurrentTime(
                    playerTime.elapsedTimeMillis,
                    playerTime.isRunning,
                    playerTime.lastStartTimeMillis,
                    currentTime,
                )
            } else {
                0L
            }
        PlayerTimeItem(
            player = player,
            timeMillis = displayTime,
            isRunning = playerTime?.isRunning ?: false,
            isPaused = playerTime?.status == PlayerTimeStatus.PAUSED,
            isCaptain = player.id == captainId,
        )
    }

internal fun calculatePlayerCurrentTime(
    elapsedTimeMillis: Long,
    isRunning: Boolean,
    lastStartTimeMillis: Long?,
    currentTimeMillis: Long,
): Long =
    if (isRunning && lastStartTimeMillis != null) {
        (elapsedTimeMillis + (currentTimeMillis - lastStartTimeMillis)).coerceAtLeast(0L)
    } else {
        elapsedTimeMillis.coerceAtLeast(0L)
    }
