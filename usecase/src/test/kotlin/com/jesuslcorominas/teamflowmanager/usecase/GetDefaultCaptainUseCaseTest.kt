package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.GetDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetDefaultCaptainUseCaseTest {
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var useCase: GetDefaultCaptainUseCase

    @Before
    fun setup() {
        preferencesRepository = mockk()
        useCase = GetDefaultCaptainUseCaseImpl(preferencesRepository)
    }

    @Test
    fun `invoke should return captain ID when set`() = runTest {
        // Given
        val captainId = 42L
        every { preferencesRepository.getDefaultCaptainId() } returns captainId

        // When
        val result = useCase.invoke()

        // Then
        assertEquals(captainId, result)
        verify { preferencesRepository.getDefaultCaptainId() }
    }

    @Test
    fun `invoke should return null when no captain is set`() = runTest {
        // Given
        every { preferencesRepository.getDefaultCaptainId() } returns null

        // When
        val result = useCase.invoke()

        // Then
        assertNull(result)
        verify { preferencesRepository.getDefaultCaptainId() }
    }
}
