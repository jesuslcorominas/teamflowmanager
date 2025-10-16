package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.first

interface SetCurrentMatchUseCase {
    suspend operator fun invoke(matchId: Long)
}

internal class SetCurrentMatchUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val getMatchByIdUseCase: GetMatchByIdUseCase,
) : SetCurrentMatchUseCase {
    override suspend fun invoke(matchId: Long) {
        // Get the match
        val match = getMatchByIdUseCase(matchId).first()
            ?: throw IllegalArgumentException("Match with id $matchId not found")

        // Update the match to mark it as the current match (in progress but not running yet)
        val updatedMatch =
            match.copy(
                status = MatchStatus.IN_PROGRESS,
                isRunning = false,
            )
        matchRepository.updateMatch(updatedMatch)
    }
}
