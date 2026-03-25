package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.ShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShouldShowInvalidSubstitutionAlertUseCaseTest {
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var useCase: ShouldShowInvalidSubstitutionAlertUseCase

    @Before
    fun setup() {
        preferencesRepository = mockk()
        useCase = ShouldShowInvalidSubstitutionAlertUseCaseImpl(preferencesRepository)
    }

    @Test
    fun `invoke should return true when alert should be shown`() {
        every { preferencesRepository.shouldShowInvalidSubstitutionAlert() } returns true

        val result = useCase.invoke()

        assertTrue(result)
    }

    @Test
    fun `invoke should return false when alert should not be shown`() {
        every { preferencesRepository.shouldShowInvalidSubstitutionAlert() } returns false

        val result = useCase.invoke()

        assertFalse(result)
    }
}
