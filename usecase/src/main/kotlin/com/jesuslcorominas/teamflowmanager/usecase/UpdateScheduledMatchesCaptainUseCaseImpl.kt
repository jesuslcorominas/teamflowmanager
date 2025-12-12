package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateScheduledMatchesCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository



internal class UpdateScheduledMatchesCaptainUseCaseImpl(
    private val matchRepository: MatchRepository,
    private val transactionRunner: TransactionRunner
) : UpdateScheduledMatchesCaptainUseCase {
    override suspend fun invoke(captainId: Long?) {
        val scheduledMatches = matchRepository.getScheduledMatches()
        transactionRunner.run {
            scheduledMatches.forEach { match ->
                matchRepository.updateMatchCaptain(match.id, captainId)
            }
        }
    }
}
