package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.SessionRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PauseSessionTimerUseCaseTest {
    private lateinit var sessionRepository: SessionRepository
    private lateinit var pauseSessionTimerUseCase: PauseSessionTimerUseCase

    @Before
    fun setup() {
        sessionRepository = mockk(relaxed = true)
        pauseSessionTimerUseCase = PauseSessionTimerUseCaseImpl(sessionRepository)
    }

    @Test
    fun `invoke should call pauseTimer on repository with current time`() =
        runTest {
            // Given
            val currentTime = 1000L

            // When
            pauseSessionTimerUseCase.invoke(currentTime)

            // Then
            coVerify { sessionRepository.pauseTimer(currentTime) }
        }
}
