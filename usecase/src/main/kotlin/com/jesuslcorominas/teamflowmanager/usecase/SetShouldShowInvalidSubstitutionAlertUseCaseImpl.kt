package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SetShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

internal class SetShouldShowInvalidSubstitutionAlertUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : SetShouldShowInvalidSubstitutionAlertUseCase {
    override fun invoke(shouldShow: Boolean) {
        preferencesRepository.setShouldShowInvalidSubstitutionAlert(shouldShow)
    }
}
