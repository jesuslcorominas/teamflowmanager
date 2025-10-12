package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.first

interface RegisterPlayerSubstitutionUseCase {
    suspend operator fun invoke(
        matchId: Long,
        playerOutId: Long,
        playerInId: Long,
        currentTimeMillis: Long,
    )
}

internal class RegisterPlayerSubstitutionUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val playerTimeRepository: PlayerTimeRepository,
    private val playerSubstitutionRepository: PlayerSubstitutionRepository,
) : RegisterPlayerSubstitutionUseCase {
    override suspend fun invoke(
        matchId: Long,
        playerOutId: Long,
        playerInId: Long,
        currentTimeMillis: Long,
    ) {
        // Get match to calculate elapsed time
        val match = matchRepository.getMatch().first()
        requireNotNull(match) { "No active match found" }

        val matchElapsedTime = if (match.isRunning && match.lastStartTimeMillis != null) {
            match.elapsedTimeMillis + (currentTimeMillis - match.lastStartTimeMillis)
        } else {
            match.elapsedTimeMillis
        }

        // Stop timer for player going out
        playerTimeRepository.pauseTimer(playerOutId, currentTimeMillis)

        // Start timer for player coming in
        playerTimeRepository.startTimer(playerInId, currentTimeMillis)

        // Record the substitution
        val substitution = PlayerSubstitution(
            matchId = matchId,
            playerOutId = playerOutId,
            playerInId = playerInId,
            substitutionTimeMillis = currentTimeMillis,
            matchElapsedTimeMillis = matchElapsedTime,
        )

        playerSubstitutionRepository.insertSubstitution(substitution)
    }
}
