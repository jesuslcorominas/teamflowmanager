package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Data class containing all timeline data for a finished match.
 */
data class MatchTimeline(
    val events: List<TimelineEvent>,
    val scoreEvolution: List<ScorePoint>,
)

/**
 * Use case to get all timeline events for a finished match.
 */
interface GetMatchTimelineUseCase {
    operator fun invoke(matchId: Long): Flow<MatchTimeline?>
}

internal class GetMatchTimelineUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val goalRepository: GoalRepository,
    private val playerSubstitutionRepository: PlayerSubstitutionRepository,
    private val playerRepository: PlayerRepository,
) : GetMatchTimelineUseCase {

    override fun invoke(matchId: Long): Flow<MatchTimeline?> {
        return combine(
            matchRepository.getMatchById(matchId),
            goalRepository.getMatchGoals(matchId),
            playerSubstitutionRepository.getMatchSubstitutions(matchId),
            playerRepository.getAllPlayers(),
        ) { match, goals, substitutions, players ->
            if (match == null) {
                null
            } else {
                val events = buildTimelineEvents(match, goals, substitutions, players)
                val scoreEvolution = buildScoreEvolution(match, goals)
                MatchTimeline(
                    events = events,
                    scoreEvolution = scoreEvolution,
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
                )
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
                    teamScore = teamScore,
                    opponentScore = opponentScore,
                )
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
                    )
                )
            }
        }

        // 4. Add period break events (for multi-period matches)
        match.periods.forEachIndexed { index, period ->
            // Add a break event at the end of each period except the last
            if (index < match.periods.size - 1 && period.endTimeMillis > 0) {
                // Calculate accumulated play time up to this period's end
                // (sum of all period durations up to and including this one)
                val accumulatedPlayTime = match.periods
                    .take(index + 1)
                    .filter { it.startTimeMillis > 0 && it.endTimeMillis > 0 }
                    .sumOf { it.endTimeMillis - it.startTimeMillis }

                events.add(
                    TimelineEvent.PeriodBreak(
                        matchElapsedTimeMillis = accumulatedPlayTime,
                        periodNumber = period.periodNumber,
                        periodType = match.periodType,
                    )
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
        points.add(ScorePoint(timeMillis = 0L, teamScore = 0, opponentScore = 0))

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
                )
            )
        }

        // Add final point at match end (total actual play time, excluding breaks)
        // This matches how matchElapsedTimeMillis is calculated for events during the match
        val totalElapsedTime = match.periods
            .filter { it.startTimeMillis > 0 && it.endTimeMillis > 0 }
            .sumOf { it.endTimeMillis - it.startTimeMillis }

        if (points.lastOrNull()?.timeMillis != totalElapsedTime) {
            points.add(
                ScorePoint(
                    timeMillis = totalElapsedTime,
                    teamScore = teamScore,
                    opponentScore = opponentScore,
                )
            )
        }

        return points
    }
}
