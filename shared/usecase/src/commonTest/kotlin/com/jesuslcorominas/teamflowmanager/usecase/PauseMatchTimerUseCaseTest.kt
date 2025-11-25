package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PauseMatchTimerUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var pauseMatchTimerUseCase: PauseMatchTimerUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        pauseMatchTimerUseCase = PauseMatchTimerUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should call pauseTimer on repository with current time`() =
        runTest {
            // Given
            val currentTime = 1000L

            // When
            pauseMatchTimerUseCase.invoke(currentTime)

            // Then
            coVerify { matchRepository.pauseTimer(currentTime) }
        }
}
