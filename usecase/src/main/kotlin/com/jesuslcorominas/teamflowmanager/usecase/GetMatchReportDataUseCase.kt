package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.GoalReport
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerMatchReport
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionReport
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionType
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface GetMatchReportDataUseCase {
    operator fun invoke(matchId: Long): Flow<MatchReportData?>
}

internal class GetMatchReportDataUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val playerRepository: PlayerRepository,
    private val playerTimeHistoryRepository: PlayerTimeHistoryRepository,
    private val goalRepository: GoalRepository,
    private val playerSubstitutionRepository: PlayerSubstitutionRepository,
) : GetMatchReportDataUseCase {
    override fun invoke(matchId: Long): Flow<MatchReportData?> {
        return combine(
            matchRepository.getMatchById(matchId),
            playerRepository.getAllPlayers(),
            playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId),
            goalRepository.getMatchGoals(matchId),
            playerSubstitutionRepository.getMatchSubstitutions(matchId),
        ) { match, players, playerTimes, goals, substitutions ->
            if (match == null) {
                null
            } else {
                // Get all players who participated in the match (squadCallUpIds)
                val matchPlayers = players.filter { match.squadCallUpIds.contains(it.id) }
                
                val playerReports = matchPlayers.map { player ->
                    // Get player time
                    val playerTime = playerTimes.find { it.playerId == player.id }
                    val totalPlayTimeMillis = playerTime?.elapsedTimeMillis ?: 0L
                    
                    // Get player goals (only team goals, not opponent goals)
                    val playerGoals = goals
                        .filter { it.scorerId == player.id && !it.isOpponentGoal }
                        .map { GoalReport(it.matchElapsedTimeMillis, it.isOwnGoal) }
                        .sortedBy { it.matchElapsedTimeMillis }
                    
                    // Get player substitutions
                    val playerSubstitutions = mutableListOf<SubstitutionReport>()
                    substitutions.forEach { sub ->
                        if (sub.playerOutId == player.id) {
                            playerSubstitutions.add(
                                SubstitutionReport(
                                    type = SubstitutionType.OUT,
                                    matchElapsedTimeMillis = sub.matchElapsedTimeMillis
                                )
                            )
                        }
                        if (sub.playerInId == player.id) {
                            playerSubstitutions.add(
                                SubstitutionReport(
                                    type = SubstitutionType.IN,
                                    matchElapsedTimeMillis = sub.matchElapsedTimeMillis
                                )
                            )
                        }
                    }
                    playerSubstitutions.sortBy { it.matchElapsedTimeMillis }
                    
                    PlayerMatchReport(
                        player = player,
                        number = player.number,
                        isGoalkeeper = player.positions.contains(Position.Goalkeeper),
                        isCaptain = player.id == match.captainId,
                        isStarter = match.startingLineupIds.contains(player.id),
                        totalPlayTimeMillis = totalPlayTimeMillis,
                        goals = playerGoals,
                        substitutions = playerSubstitutions,
                    )
                }
                
                // Build timeline events and score evolution for the report
                val timelineEvents = buildTimelineEvents(match, goals, substitutions, players)
                val scoreEvolution = buildScoreEvolution(match, goals)
                
                MatchReportData(
                    match = match,
                    playerReports = playerReports.sortedBy { it.number },
                    timelineEvents = timelineEvents,
                    scoreEvolution = scoreEvolution,
                )
            }
        }
    }

    /**
     * Builds a list of timeline events for a match.
     * Events include starting lineup, goals, substitutions, and period breaks,
     * sorted chronologically by match elapsed time.
     *
     * @param match The match data containing period information
     * @param goals List of goals scored during the match
     * @param substitutions List of player substitutions during the match
     * @param players List of all players to resolve player references
     * @return List of timeline events sorted by elapsed time
     */
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
            if (index < match.periods.size - 1 && isCompletedPeriod(period)) {
                // Calculate accumulated play time up to this period's end
                val accumulatedPlayTime = match.periods
                    .take(index + 1)
                    .filter { isCompletedPeriod(it) }
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

        // Sort by time in ascending order
        return events.sortedBy { it.matchElapsedTimeMillis }
    }

    /**
     * Builds a list of score points representing the evolution of the score during the match.
     * Used to render the score evolution chart in the PDF report.
     *
     * @param match The match data containing period information for total elapsed time
     * @param goals List of goals scored during the match
     * @return List of score points including start (0-0), all goals, and final score
     */
    private fun buildScoreEvolution(
        match: Match,
        goals: List<Goal>,
    ): List<ScorePoint> {
        val points = mutableListOf<ScorePoint>()

        // Start at 0-0
        points.add(ScorePoint(timeMillis = 0L, teamScore = 0, opponentScore = 0, isOpponentGoal = false))

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
                    isOpponentGoal = goal.isOpponentGoal
                )
            )
        }

        // Add final point at match end
        val totalElapsedTime = calculateTotalElapsedTime(match)

        if (points.lastOrNull()?.timeMillis != totalElapsedTime) {
            points.add(
                ScorePoint(
                    timeMillis = totalElapsedTime,
                    teamScore = teamScore,
                    opponentScore = opponentScore,
                    isOpponentGoal = false
                )
            )
        }

        return points
    }

    /**
     * Checks if a match period has been completed (has valid start and end times).
     */
    private fun isCompletedPeriod(period: MatchPeriod): Boolean =
        period.startTimeMillis > 0 && period.endTimeMillis > 0

    private fun calculateTotalElapsedTime(match: Match): Long {
        return match.periods
            .filter { isCompletedPeriod(it) }
            .sumOf { it.endTimeMillis - it.startTimeMillis }
    }
}
