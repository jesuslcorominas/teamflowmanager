package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.SessionRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StartSessionTimerUseCaseTest {
    private lateinit var sessionRepository: SessionRepository
    private lateinit var startSessionTimerUseCase: StartSessionTimerUseCase

    @Before
    fun setup() {
        sessionRepository = mockk(relaxed = true)
        startSessionTimerUseCase = StartSessionTimerUseCaseImpl(sessionRepository)
    }

    @Test
    fun `invoke should call startTimer on repository with current time`() =
        runTest {
            // Given
            val currentTime = 1000L

            // When
            startSessionTimerUseCase.invoke(currentTime)

            // Then
            coVerify { sessionRepository.startTimer(currentTime) }
        }
}
