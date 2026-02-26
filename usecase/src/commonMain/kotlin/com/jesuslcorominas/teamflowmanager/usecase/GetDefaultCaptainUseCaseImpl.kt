package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.GetDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository



internal class GetDefaultCaptainUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : GetDefaultCaptainUseCase {
    override suspend fun invoke(): Long? = preferencesRepository.getDefaultCaptainId()
}
