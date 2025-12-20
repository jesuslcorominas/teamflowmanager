package com.jesuslcorominas.teamflowmanager.domain.usecase

interface GetDefaultCaptainUseCase {
    suspend operator fun invoke(): Long?
}
