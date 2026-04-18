package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerActivityInterval
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
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

data class EndPeriodState(
    val isBreak: Boolean,
)

internal fun List<Player>.toPlayerItems(
    playerTimes: List<PlayerTime>,
    currentTime: Long,
    captainId: Long,
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
