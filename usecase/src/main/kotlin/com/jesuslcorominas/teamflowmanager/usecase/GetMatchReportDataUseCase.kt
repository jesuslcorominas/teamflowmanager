package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.GoalReport
import com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerMatchReport
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionReport
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionType
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
                    
                    // Get player goals: regular goals (not opponent goals) or own goals attributed to this player
                    val playerGoals = goals
                        .filter { it.scorerId == player.id && (!it.isOpponentGoal || it.isOwnGoal) }
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
                
                MatchReportData(
                    match = match,
                    playerReports = playerReports.sortedBy { it.number }
                )
            }
        }
    }
}
