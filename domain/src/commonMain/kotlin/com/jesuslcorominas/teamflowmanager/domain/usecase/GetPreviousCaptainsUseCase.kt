package com.jesuslcorominas.teamflowmanager.domain.usecase

interface GetPreviousCaptainsUseCase {
    suspend operator fun invoke(count: Int = 2): List<Long?>
}
