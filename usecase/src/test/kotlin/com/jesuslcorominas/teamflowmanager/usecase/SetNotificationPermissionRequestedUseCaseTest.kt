package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SetNotificationPermissionRequestedUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SetNotificationPermissionRequestedUseCaseTest {
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var useCase: SetNotificationPermissionRequestedUseCase

    @Before
    fun setup() {
        preferencesRepository = mockk(relaxed = true)
        useCase = SetNotificationPermissionRequestedUseCaseImpl(preferencesRepository)
    }

    @Test
    fun `invoke with true should call setNotificationPermissionRequested with true`() {
        useCase.invoke(true)
        verify { preferencesRepository.setNotificationPermissionRequested(true) }
    }

    @Test
    fun `invoke with false should call setNotificationPermissionRequested with false`() {
        useCase.invoke(false)
        verify { preferencesRepository.setNotificationPermissionRequested(false) }
    }
}
