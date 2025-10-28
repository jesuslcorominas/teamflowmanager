package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
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
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val transactionRunner: TransactionRunner
) : RegisterPlayerSubstitutionUseCase {
    override suspend fun invoke(
        matchId: Long,
        playerOutId: Long,
        playerInId: Long,
        currentTimeMillis: Long,
    ) {
        // Get match to calculate elapsed time
        val match = matchRepository.getMatchById(matchId).first()
        requireNotNull(match) { "No active match found" }

        // Get all player times to check if playerOut is actually playing
        val playerTimes = getAllPlayerTimesUseCase().first()
        val playerOutTime = playerTimes.find { it.playerId == playerOutId }

        // Only proceed if the player being substituted out is currently playing (status PLAYING)
        if (playerOutTime?.status != PlayerTimeStatus.PLAYING) {
            return
        }

        val matchElapsedTime = match.getTotalElapsed(currentTimeMillis)

        transactionRunner.run {
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
}
