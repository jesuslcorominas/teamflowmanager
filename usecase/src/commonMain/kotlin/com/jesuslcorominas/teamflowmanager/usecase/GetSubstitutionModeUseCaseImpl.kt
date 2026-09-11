package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetSubstitutionModeUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GetSubstitutionModeUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : GetSubstitutionModeUseCase {
    override fun invoke(): Flow<SubstitutionMode> =
        preferencesRepository.observeSubstitutionMode().map { stored ->
            when (stored) {
                SubstitutionMode.LIVE.name -> SubstitutionMode.LIVE
                // Unknown or absent value: the product default.
                else -> SubstitutionMode.SCHEDULED
            }
        }
}
