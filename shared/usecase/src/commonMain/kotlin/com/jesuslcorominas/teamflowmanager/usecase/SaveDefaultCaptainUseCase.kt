package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

interface SaveDefaultCaptainUseCase {
    operator fun invoke(playerId: Long?)
}

internal class SaveDefaultCaptainUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : SaveDefaultCaptainUseCase {
    override fun invoke(playerId: Long?) {
        preferencesRepository.setDefaultCaptainId(playerId)
    }
}
