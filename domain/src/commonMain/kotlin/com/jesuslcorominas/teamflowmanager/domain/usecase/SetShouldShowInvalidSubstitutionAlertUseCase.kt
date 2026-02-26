package com.jesuslcorominas.teamflowmanager.domain.usecase

interface SetShouldShowInvalidSubstitutionAlertUseCase {
    operator fun invoke(shouldShow: Boolean)
}
