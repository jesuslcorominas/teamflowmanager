package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.first

interface StartMatchUseCase {
    suspend operator fun invoke(matchId: Long, currentTimeMillis: Long)
}

internal class StartMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val getMatchByIdUseCase: GetMatchByIdUseCase,
    private val startPlayerTimerUseCase: StartPlayerTimerUseCase,
) : StartMatchUseCase {
    override suspend fun invoke(
        matchId: Long,
        currentTimeMillis: Long,
    ) {
        // Get the match to start
        val match = getMatchByIdUseCase(matchId).first()
            ?: throw IllegalArgumentException("Match with id $matchId not found")

        // Update the match to mark it as running
        val updatedMatch =
            match.copy(
                isRunning = true,
                lastStartTimeMillis = currentTimeMillis,
            )
        matchRepository.updateMatch(updatedMatch)

        // Start timers for all players in the starting lineup
        updatedMatch.startingLineupIds.forEach { playerId ->
            startPlayerTimerUseCase(playerId, currentTimeMillis)
        }
    }
}
