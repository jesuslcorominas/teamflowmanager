package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchTimeline
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerActivityInterval
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchTimelineUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class GetMatchTimelineUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val goalRepository: GoalRepository,
    private val playerSubstitutionRepository: PlayerSubstitutionRepository,
    private val playerRepository: PlayerRepository,
) : GetMatchTimelineUseCase {
    override fun invoke(
        matchId: Long,
        teamId: String?,
    ): Flow<MatchTimeline?> {
        return combine(
            matchRepository.getMatchById(matchId, teamId),
            goalRepository.getMatchGoals(matchId, teamId),
            playerSubstitutionRepository.getMatchSubstitutions(matchId, teamId),
            if (teamId != null) playerRepository.getPlayersByTeam(teamId) else playerRepository.getAllPlayers(),
        ) { match, goals, substitutions, players ->
            if (match == null) {
                null
            } else {
                val events = buildTimelineEvents(match, goals, substitutions, players)
                val scoreEvolution = buildScoreEvolution(match, goals)
                val playerActivity = buildPlayerActivity(match, substitutions, players)
                MatchTimeline(
                    events = events,
                    scoreEvolution = scoreEvolution,
                    playerActivity = playerActivity,
                )
            }
        }
    }

    private fun buildTimelineEvents(
        match: Match,
        goals: List<Goal>,
        substitutions: List<PlayerSubstitution>,
        players: List<Player>,
    ): List<TimelineEvent> {
        val events = mutableListOf<TimelineEvent>()

        // 1. Add starting lineup event
        val startingPlayers = players.filter { it.id in match.startingLineupIds }
        if (startingPlayers.isNotEmpty()) {
            events.add(
                TimelineEvent.StartingLineup(
                    matchElapsedTimeMillis = 0L,
                    players = startingPlayers.sortedBy { it.number },
                ),
            )
        }

        // 2. Add goal events with running score
        var teamScore = 0
        var opponentScore = 0
        goals.sortedBy { it.matchElapsedTimeMillis }.forEach { goal ->
            if (goal.isOpponentGoal) {
                opponentScore++
            } else {
                teamScore++
            }
            events.add(
                TimelineEvent.GoalScored(
                    matchElapsedTimeMillis = goal.matchElapsedTimeMillis,
                    scorer = if (goal.scorerId != null) players.find { it.id == goal.scorerId } else null,
                    isOpponentGoal = goal.isOpponentGoal,
                    isOwnGoal = goal.isOwnGoal,
                    teamScore = teamScore,
                    opponentScore = opponentScore,
                ),
            )
        }

        // 3. Add substitution events
        substitutions.forEach { substitution ->
            val playerIn = players.find { it.id == substitution.playerInId }
            val playerOut = players.find { it.id == substitution.playerOutId }
            if (playerIn != null && playerOut != null) {
                events.add(
                    TimelineEvent.Substitution(
                        matchElapsedTimeMillis = substitution.matchElapsedTimeMillis,
                        playerIn = playerIn,
                        playerOut = playerOut,
                    ),
                )
            }
        }

        // 4. Add period break events (for multi-period matches)
        match.periods.forEachIndexed { index, period ->
            // Add a break event at the end of each period except the last
            if (index < match.periods.size - 1 && period.endTimeMillis > 0) {
                // Calculate accumulated play time up to this period's end
                // (sum of all period durations up to and including this one)
                val accumulatedPlayTime =
                    match.periods
                        .take(index + 1)
                        .filter { it.startTimeMillis > 0 && it.endTimeMillis > 0 }
                        .sumOf { it.endTimeMillis - it.startTimeMillis }

                events.add(
                    TimelineEvent.PeriodBreak(
                        matchElapsedTimeMillis = accumulatedPlayTime,
                        periodNumber = period.periodNumber,
                        periodType = match.periodType,
                    ),
                )
            }
        }

        // Sort by time in ascending order (first events at top, last events at bottom)
        return events.sortedBy { it.matchElapsedTimeMillis }
    }

    private fun buildScoreEvolution(
        match: Match,
        goals: List<Goal>,
    ): List<ScorePoint> {
        val points = mutableListOf<ScorePoint>()

        // Start at 0-0
        points.add(ScorePoint(timeMillis = 0L, teamScore = 0, opponentScore = 0, isOpponentGoal = false))
        points.add(ScorePoint(timeMillis = 0L, teamScore = 0, opponentScore = 0, isOpponentGoal = true))

        // Add a point for each goal
        var teamScore = 0
        var opponentScore = 0
        goals.sortedBy { it.matchElapsedTimeMillis }.forEach { goal ->
            if (goal.isOpponentGoal) {
                opponentScore++
            } else {
                teamScore++
            }
            points.add(
                ScorePoint(
                    timeMillis = goal.matchElapsedTimeMillis,
                    teamScore = teamScore,
                    opponentScore = opponentScore,
                    isOpponentGoal = goal.isOpponentGoal,
                ),
            )
        }

        // Add final points at match end (total actual play time, excluding breaks)
        // This matches how matchElapsedTimeMillis is calculated for events during the match
        val totalElapsedTime = calculateTotalElapsedTime(match)

        if (points.lastOrNull()?.timeMillis != totalElapsedTime) {
            points.add(
                ScorePoint(
                    timeMillis = totalElapsedTime,
                    teamScore = teamScore,
                    opponentScore = opponentScore,
                    isOpponentGoal = false,
                ),
            )

            points.add(
                ScorePoint(
                    timeMillis = totalElapsedTime,
                    teamScore = teamScore,
                    opponentScore = opponentScore,
                    isOpponentGoal = true,
                ),
            )
        }

        return points
    }

    /**
     * Calculates the total elapsed time for a match based on completed periods.
     */
    private fun calculateTotalElapsedTime(match: Match): Long {
        return match.periods
            .filter { it.startTimeMillis > 0 && it.endTimeMillis > 0 }
            .sumOf { it.endTimeMillis - it.startTimeMillis }
    }

    /**
     * Build player activity intervals showing when each player was on the field.
     * Uses starting lineup and substitutions to compute time intervals.
     */
    private fun buildPlayerActivity(
        match: Match,
        substitutions: List<PlayerSubstitution>,
        players: List<Player>,
    ): List<PlayerActivityInterval> {
        val intervals = mutableListOf<PlayerActivityInterval>()

        // Calculate total match time
        val totalElapsedTime = calculateTotalElapsedTime(match)

        // Track which players are currently active and their start time
        val activePlayerStartTimes = mutableMapOf<Long, Long>()

        // Starting lineup players are active from time 0
        match.startingLineupIds.forEach { playerId ->
            activePlayerStartTimes[playerId] = 0L
        }

        // Process substitutions in chronological order
        substitutions.sortedBy { it.matchElapsedTimeMillis }.forEach { substitution ->
            // Player out: end their interval
            val playerOutId = substitution.playerOutId
            val playerOutStartTime = activePlayerStartTimes.remove(playerOutId)
            if (playerOutStartTime != null) {
                val player = players.find { it.id == playerOutId }
                if (player != null) {
                    intervals.add(
                        PlayerActivityInterval(
                            player = player,
                            startTimeMillis = playerOutStartTime,
                            endTimeMillis = substitution.matchElapsedTimeMillis,
                        ),
                    )
                }
            }

            // Player in: start their interval
            activePlayerStartTimes[substitution.playerInId] = substitution.matchElapsedTimeMillis
        }

        // End intervals for players still active at match end
        activePlayerStartTimes.forEach { (playerId, startTime) ->
            val player = players.find { it.id == playerId }
            if (player != null) {
                intervals.add(
                    PlayerActivityInterval(
                        player = player,
                        startTimeMillis = startTime,
                        endTimeMillis = totalElapsedTime,
                    ),
                )
            }
        }

        // Sort by player number for consistent display
        return intervals.sortedBy { it.player.number }
    }
}
