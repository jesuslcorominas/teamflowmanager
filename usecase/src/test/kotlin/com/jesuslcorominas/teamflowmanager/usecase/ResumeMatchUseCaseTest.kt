package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResumeMatchUseCaseTest {
    private lateinit var startMatchTimerUseCase: StartMatchTimerUseCase
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var startPlayerTimerUseCase: StartPlayerTimerUseCase
    private lateinit var resumeMatchUseCase: ResumeMatchUseCase

    @Before
    fun setup() {
        startMatchTimerUseCase = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        startPlayerTimerUseCase = mockk(relaxed = true)
        resumeMatchUseCase =
            ResumeMatchUseCaseImpl(
                startMatchTimerUseCase,
                getAllPlayerTimesUseCase,
                startPlayerTimerUseCase,
            )
    }

    @Test
    fun `invoke should resume match timer and all player timers with elapsed time`() =
        runTest {
            // Given
            val currentTime = 1000L
            val pausedPlayerTimes =
                listOf(
                    PlayerTime(playerId = 1L, isRunning = false, elapsedTimeMillis = 500L),
                    PlayerTime(playerId = 2L, isRunning = false, elapsedTimeMillis = 300L),
                    PlayerTime(playerId = 3L, isRunning = false, elapsedTimeMillis = 0L),
                )

            coEvery { getAllPlayerTimesUseCase() } returns flowOf(pausedPlayerTimes)

            // When
            resumeMatchUseCase.invoke(currentTime)

            // Then
            coVerify { startMatchTimerUseCase(currentTime) }
            coVerify { startPlayerTimerUseCase(1L, currentTime) }
            coVerify { startPlayerTimerUseCase(2L, currentTime) }
            coVerify(exactly = 0) { startPlayerTimerUseCase(3L, any()) }
        }

    @Test
    fun `invoke should resume match timer even when no player times have elapsed time`() =
        runTest {
            // Given
            val currentTime = 1000L
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, isRunning = false, elapsedTimeMillis = 0L),
                    PlayerTime(playerId = 2L, isRunning = false, elapsedTimeMillis = 0L),
                )

            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            // When
            resumeMatchUseCase.invoke(currentTime)

            // Then
            coVerify { startMatchTimerUseCase(currentTime) }
            coVerify(exactly = 0) { startPlayerTimerUseCase(any(), any()) }
        }

    @Test
    fun `invoke should resume match timer when no player times exist`() =
        runTest {
            // Given
            val currentTime = 1000L
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(emptyList())

            // When
            resumeMatchUseCase.invoke(currentTime)

            // Then
            coVerify { startMatchTimerUseCase(currentTime) }
            coVerify(exactly = 0) { startPlayerTimerUseCase(any(), any()) }
        }
}
