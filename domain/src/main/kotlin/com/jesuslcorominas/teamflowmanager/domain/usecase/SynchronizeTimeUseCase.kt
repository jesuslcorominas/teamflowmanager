package com.jesuslcorominas.teamflowmanager.domain.usecase

/**
 * Use case to synchronize time with the server.
 * Should be called before starting a match or periodically to maintain accuracy.
 */
interface SynchronizeTimeUseCase {
    suspend operator fun invoke()
}
