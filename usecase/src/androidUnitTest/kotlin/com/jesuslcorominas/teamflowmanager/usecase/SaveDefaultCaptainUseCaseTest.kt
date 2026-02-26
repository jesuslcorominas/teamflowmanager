package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SaveDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SaveDefaultCaptainUseCaseTest {
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var useCase: SaveDefaultCaptainUseCase

    @Before
    fun setup() {
        preferencesRepository = mockk()
        useCase = SaveDefaultCaptainUseCaseImpl(preferencesRepository)
    }

    @Test
    fun `invoke should save captain ID`() {
        // Given
        val captainId = 42L
        every { preferencesRepository.setDefaultCaptainId(captainId) } just runs

        // When
        useCase.invoke(captainId)

        // Then
        verify { preferencesRepository.setDefaultCaptainId(captainId) }
    }

    @Test
    fun `invoke should save null to clear default captain`() {
        // Given
        every { preferencesRepository.setDefaultCaptainId(null) } just runs

        // When
        useCase.invoke(null)

        // Then
        verify { preferencesRepository.setDefaultCaptainId(null) }
    }
}
