package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider

/**
 * Use case to synchronize time with the server.
 * Should be called before starting a match or periodically to maintain accuracy.
 */
interface SynchronizeTimeUseCase {
    suspend operator fun invoke()
}

internal class SynchronizeTimeUseCaseImpl(
    private val timeProvider: TimeProvider
) : SynchronizeTimeUseCase {
    override suspend fun invoke() {
        timeProvider.synchronize()
    }
}
