package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SynchronizeTimeUseCaseTest {
    private lateinit var timeProvider: TimeProvider
    private lateinit var synchronizeTimeUseCase: SynchronizeTimeUseCase

    @Before
    fun setup() {
        timeProvider = mockk(relaxed = true)
        synchronizeTimeUseCase = SynchronizeTimeUseCaseImpl(timeProvider)
    }

    @Test
    fun `invoke should call synchronize on timeProvider`() =
        runTest {
            // When
            synchronizeTimeUseCase.invoke()

            // Then
            coVerify { timeProvider.synchronize() }
        }
}
