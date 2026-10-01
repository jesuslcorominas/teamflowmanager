package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchTimelineUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersByTeamUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Assembles [MatchUiState] out of the five flows that describe a match: the match itself, the
 * player times, the squad, the clock and the timeline — plus the summary once it has finished.
 *
 * Moved here unchanged from the ViewModel, nested collect included. It is the most intricate part
 * of the screen — the operation-id filter that stops the UI flickering mid-substitution lives in
 * it — and rewriting it while moving it would have made the change impossible to review.
 */
internal class MatchStateLoader(
    private val getMatchById: GetMatchByIdUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val getPlayersByTeamUseCase: GetPlayersByTeamUseCase,
    private val getMatchTimelineUseCase: GetMatchTimelineUseCase,
    private val getMatchSummaryUseCase: GetMatchSummaryUseCase,
) {
    /** The call-up, which is who may appear on the pitch or on a pending substitution card. */
    fun squadPlayers(
        matchId: String,
        scope: CoroutineScope,
    ): StateFlow<List<Player>> =
        getMatchById(matchId)
            .flatMapLatest { match ->
                if (match == null) {
                    flowOf(emptyList())
                } else {
                    getPlayersByTeamUseCase(match.teamId)
                        .map { players -> players.filter { it.id in match.squadCallUpIds } }
                }
            }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Collects until cancelled, handing every new state to [emit]. */
    suspend fun load(
        matchId: String,
        currentTimeFlow: StateFlow<Long>,
        emit: (MatchUiState) -> Unit,
    ) {
        combine(
            getMatchById(matchId),
            getAllPlayerTimesUseCase(matchId),
            getMatchById(matchId).flatMapLatest { match ->
                if (match == null) flowOf(emptyList()) else getPlayersByTeamUseCase(match.teamId)
            },
            currentTimeFlow,
            getMatchTimelineUseCase(matchId),
        ) { match, playerTimes, players, currentTime, timeline ->
            when {
                match == null -> MatchUiState.NoMatch
                match.status == MatchStatus.FINISHED -> {
                    // Match is finished, load summary from history
                    null // Will be handled separately
                }

                else -> {
                    // Only include players that are in the squad call-up
                    val squadPlayers = players.filter { it.id in match.squadCallUpIds }

                    // Filter player times to show only those from completed operations
                    // This prevents UI flicker during multi-step atomic operations
                    val filteredPlayerTimes =
                        if (match.lastCompletedOperationId != null) {
                            playerTimes.filter { playerTime ->
                                // Show players whose lastOperationId matches the match's last completed operation
                                // OR has null operationId (backward compatibility for pre-operation-tracking data)
                                // OR is ON_BENCH — bench players are not updated during substitutions involving
                                // other players, so their lastOperationId may be stale; always show them.
                                playerTime.lastOperationId == match.lastCompletedOperationId ||
                                    playerTime.lastOperationId == null ||
                                    playerTime.status == PlayerTimeStatus.ON_BENCH
                            }
                        } else {
                            // No operations completed yet, show all player times
                            playerTimes
                        }

                    val playerTimeItems =
                        squadPlayers.toPlayerItems(
                            filteredPlayerTimes,
                            currentTime,
                            match.captainId,
                        )

                    MatchUiState.Success(
                        match = match,
                        currentTime = currentTimeFlow.value,
                        playerTimes = playerTimeItems,
                        timelineEvents = timeline?.events ?: emptyList(),
                    )
                }
            }
        }.collect { state ->
            if (state != null) {
                emit(state)
            } else {
                // Load finished match summary and timeline
                getMatchById(matchId).collect { match ->
                    if (match != null && match.status == MatchStatus.FINISHED) {
                        combine(
                            getMatchSummaryUseCase(match.id),
                            getMatchTimelineUseCase(match.id),
                        ) { summary, timeline ->
                            if (summary != null) {
                                MatchUiState.Finished(
                                    match = match,
                                    currentTime = currentTimeFlow.value,
                                    playerTimes =
                                        summary.playerTimes.map { playerTimeSummary ->
                                            PlayerTimeItem(
                                                player = playerTimeSummary.player,
                                                timeMillis = playerTimeSummary.elapsedTimeMillis,
                                                isRunning = false,
                                                isPaused = false,
                                                isCaptain = playerTimeSummary.player.id == summary.match.captainId,
                                            )
                                        },
                                    substitutions =
                                        summary.substitutions.map { sub ->
                                            SubstitutionItem(
                                                playerOut = sub.playerOut,
                                                playerIn = sub.playerIn,
                                                matchElapsedTimeMillis = sub.matchElapsedTimeMillis,
                                            )
                                        },
                                    timelineEvents = timeline?.events ?: emptyList(),
                                    scoreEvolution = timeline?.scoreEvolution ?: emptyList(),
                                    playerActivity = timeline?.playerActivity ?: emptyList(),
                                )
                            } else {
                                null
                            }
                        }.collect { finishedState ->
                            if (finishedState != null) {
                                emit(finishedState)
                            }
                        }
                    }
                }
            }
        }
    }
}
