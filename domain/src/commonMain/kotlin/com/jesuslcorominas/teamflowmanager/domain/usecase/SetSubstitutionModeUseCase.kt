package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode

interface SetSubstitutionModeUseCase {
    operator fun invoke(mode: SubstitutionMode)
}
