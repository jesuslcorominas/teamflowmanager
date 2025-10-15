package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

interface GetDefaultCaptainUseCase {
    operator fun invoke(): Long?
}

internal class GetDefaultCaptainUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : GetDefaultCaptainUseCase {
    override fun invoke(): Long? = preferencesRepository.getDefaultCaptainId()
}
