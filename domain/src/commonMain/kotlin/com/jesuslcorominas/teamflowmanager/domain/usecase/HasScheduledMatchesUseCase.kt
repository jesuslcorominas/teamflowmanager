package com.jesuslcorominas.teamflowmanager.domain.usecase

interface HasScheduledMatchesUseCase {
    suspend operator fun invoke(): Boolean
}
