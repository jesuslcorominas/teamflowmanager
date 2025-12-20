package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider



internal class SynchronizeTimeUseCaseImpl(
    private val timeProvider: TimeProvider
) : SynchronizeTimeUseCase {
    override suspend fun invoke() {
        timeProvider.synchronize()
    }
}
