package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ExportData
import com.jesuslcorominas.teamflowmanager.domain.model.MatchExportResult
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerExportStats
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetExportDataUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine



internal class GetExportDataUseCaseImpl(
    private val playerRepository: PlayerRepository,
    private val matchRepository: MatchRepository,
    private val playerTimeHistoryRepository: PlayerTimeHistoryRepository,
    private val goalRepository: GoalRepository,
) : GetExportDataUseCase {
    override fun invoke(): Flow<ExportData> {
        return combine(
            playerRepository.getAllPlayers(),
            matchRepository.getAllMatches(),
            playerTimeHistoryRepository.getAllPlayerTimeHistory(),
            goalRepository.getAllTeamGoals()
        ) { players, matches, timeHistory, goals ->
            // Calculate player stats
            val playerStats = players.map { player ->
                val finishedMatches = matches.filter {
                    it.status == com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus.FINISHED
                }

                val matchesCalledUp = finishedMatches.count { match ->
                    match.squadCallUpIds.contains(player.id)
                }

                val playerHistory = timeHistory.filter { it.playerId == player.id }
                val matchesPlayed = playerHistory.distinctBy { it.matchId }.size
                val totalTime = (playerHistory.sumOf { it.elapsedTimeMillis }.toDouble()) / (60 * 1000)
                val averageTime = if (matchesPlayed > 0) totalTime / matchesPlayed else 0.0

                val playerGoals = goals.filter { it.scorerId == player.id }
                val goalsScored = playerGoals.size

                PlayerExportStats(
                    player = player,
                    matchesCalledUp = matchesCalledUp,
                    matchesPlayed = matchesPlayed,
                    totalTimeMinutes = totalTime,
                    averageTimePerMatch = averageTime,
                    goalsScored = goalsScored
                )
            }.sortedByDescending { it.totalTimeMinutes }

            // Calculate top scorers
            val topScorers = players.map { player ->
                val playerGoals = goals.filter { it.scorerId == player.id }
                val totalGoals = playerGoals.size
                val matchesWithGoals = playerGoals.distinctBy { it.matchId }.size

                com.jesuslcorominas.teamflowmanager.domain.model.PlayerGoalStats(
                    player = player,
                    totalGoals = totalGoals,
                    matchesWithGoals = matchesWithGoals
                )
            }.filter { it.totalGoals > 0 }
            .sortedByDescending { it.totalGoals }

            // Calculate match results
            val matchResults = matches
                .filter { it.status == com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus.FINISHED }
                .mapNotNull { match ->
                    match.dateTime?.let { date ->
                        MatchExportResult(
                            match = match,
                            date = date,
                            opponent = match.opponent,
                            location = match.location,
                            teamGoals = match.goals,
                            opponentGoals = match.opponentGoals
                        )
                    }
                }
                .sortedBy { it.date }

            ExportData(
                playerStats = playerStats,
                topScorers = topScorers,
                matchResults = matchResults
            )
        }
    }
}
