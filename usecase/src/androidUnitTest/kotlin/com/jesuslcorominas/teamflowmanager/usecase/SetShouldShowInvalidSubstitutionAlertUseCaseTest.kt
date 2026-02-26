package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SetShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SetShouldShowInvalidSubstitutionAlertUseCaseTest {
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var useCase: SetShouldShowInvalidSubstitutionAlertUseCase

    @Before
    fun setup() {
        preferencesRepository = mockk(relaxed = true)
        useCase = SetShouldShowInvalidSubstitutionAlertUseCaseImpl(preferencesRepository)
    }

    @Test
    fun `invoke with true should call setShouldShowInvalidSubstitutionAlert with true`() {
        useCase.invoke(true)
        verify { preferencesRepository.setShouldShowInvalidSubstitutionAlert(true) }
    }

    @Test
    fun `invoke with false should call setShouldShowInvalidSubstitutionAlert with false`() {
        useCase.invoke(false)
        verify { preferencesRepository.setShouldShowInvalidSubstitutionAlert(false) }
    }
}
