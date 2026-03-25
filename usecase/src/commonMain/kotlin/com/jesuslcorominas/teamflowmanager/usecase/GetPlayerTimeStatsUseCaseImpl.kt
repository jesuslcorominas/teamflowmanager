package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStats
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerTimeStatsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetPlayerTimeStatsUseCaseImpl(
    private val playerRepository: PlayerRepository,
    private val playerTimeHistoryRepository: PlayerTimeHistoryRepository,
) : GetPlayerTimeStatsUseCase {
    override operator fun invoke(): Flow<List<PlayerTimeStats>> {
        return combine(
            playerRepository.getAllPlayers(),
            playerTimeHistoryRepository.getAllPlayerTimeHistory(),
        ) { players, timeHistory ->
            players.map { player ->
                val playerHistory = timeHistory.filter { it.playerId == player.id }
                val totalTime = (playerHistory.sumOf { it.elapsedTimeMillis }.toDouble()) / (60 * 1000)
                val matchesPlayed = playerHistory.distinctBy { it.matchId }.size

                PlayerTimeStats(
                    player = player,
                    totalTimeMinutes = totalTime,
                    matchesPlayed = matchesPlayed,
                )
            }.sortedByDescending { it.totalTimeMinutes }
        }
    }
}
