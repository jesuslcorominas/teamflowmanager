package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerGoalStats
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetPlayerGoalStatsUseCase(
    private val playerRepository: PlayerRepository,
    private val goalRepository: GoalRepository,
) {
    operator fun invoke(): Flow<List<PlayerGoalStats>> {
        return combine(
            playerRepository.getAllPlayers(),
            goalRepository.getAllTeamGoals()
        ) { players, goals ->
            players.map { player ->
                val playerGoals = goals.filter { it.scorerId == player.id }
                val totalGoals = playerGoals.size

                val matchesWithGoals = playerGoals.distinctBy { it.matchId }.size
                PlayerGoalStats(
                    player = player,
                    totalGoals = totalGoals,
                    matchesWithGoals = matchesWithGoals
                )
            }.sortedByDescending { it.totalGoals }
        }
    }
}
