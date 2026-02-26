package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SaveDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

internal class SaveDefaultCaptainUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : SaveDefaultCaptainUseCase {
    override fun invoke(playerId: Long?) {
        preferencesRepository.setDefaultCaptainId(playerId)
    }
}
