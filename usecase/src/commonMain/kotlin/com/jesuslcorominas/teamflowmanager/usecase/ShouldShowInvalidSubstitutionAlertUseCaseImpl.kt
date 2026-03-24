package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.ShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

internal class ShouldShowInvalidSubstitutionAlertUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : ShouldShowInvalidSubstitutionAlertUseCase {
    override fun invoke(): Boolean {
        return preferencesRepository.shouldShowInvalidSubstitutionAlert()
    }
}
