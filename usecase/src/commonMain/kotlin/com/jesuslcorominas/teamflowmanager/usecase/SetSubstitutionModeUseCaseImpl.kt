package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetSubstitutionModeUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository

internal class SetSubstitutionModeUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : SetSubstitutionModeUseCase {
    override fun invoke(mode: SubstitutionMode) {
        preferencesRepository.setSubstitutionMode(mode.name)
    }
}
