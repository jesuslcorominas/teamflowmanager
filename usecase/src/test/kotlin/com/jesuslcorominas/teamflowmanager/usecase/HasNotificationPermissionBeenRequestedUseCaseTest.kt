package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.HasNotificationPermissionBeenRequestedUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HasNotificationPermissionBeenRequestedUseCaseTest {
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var useCase: HasNotificationPermissionBeenRequestedUseCase

    @Before
    fun setup() {
        preferencesRepository = mockk()
        useCase = HasNotificationPermissionBeenRequestedUseCaseImpl(preferencesRepository)
    }

    @Test
    fun `invoke should return true when permission has been requested`() {
        every { preferencesRepository.hasNotificationPermissionBeenRequested() } returns true

        val result = useCase.invoke()

        assertTrue(result)
    }

    @Test
    fun `invoke should return false when permission has not been requested`() {
        every { preferencesRepository.hasNotificationPermissionBeenRequested() } returns false

        val result = useCase.invoke()

        assertFalse(result)
    }
}
