package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObserveSubstitutionModeUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ObserveSubstitutionModeUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : ObserveSubstitutionModeUseCase {
    override fun invoke(): Flow<SubstitutionMode> =
        preferencesRepository.observeSubstitutionMode().map { stored ->
            when (stored) {
                SubstitutionMode.LIVE.name -> SubstitutionMode.LIVE
                // Unknown or absent value: the product default.
                else -> SubstitutionMode.SCHEDULED
            }
        }
}
