package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StartMatchTimerUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var startMatchTimerUseCase: StartMatchTimerUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        startMatchTimerUseCase = StartMatchTimerUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should call startTimer on repository with current time`() =
        runTest {
            // Given
            val currentTime = 1000L
            val matchId = 1L

            // When
            startMatchTimerUseCase.invoke(matchId, currentTime)

            // Then
            coVerify { matchRepository.startTimer(matchId, currentTime) }
        }
}
