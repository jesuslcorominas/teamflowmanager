package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PauseMatchUseCaseTest {
    private lateinit var pauseMatchTimerUseCase: PauseMatchTimerUseCase
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var pausePlayerTimerUseCase: PausePlayerTimerUseCase
    private lateinit var pauseMatchUseCase: PauseMatchUseCase

    private lateinit var pausePlayerTimerForMatchPauseUseCase: PausePlayerTimerForMatchPauseUseCase

    @Before
    fun setup() {
        pauseMatchTimerUseCase = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        pausePlayerTimerUseCase = mockk(relaxed = true)
        pausePlayerTimerForMatchPauseUseCase = mockk(relaxed = true)
        pauseMatchUseCase =
            PauseMatchUseCaseImpl(
                pauseMatchTimerUseCase,
                getAllPlayerTimesUseCase,
                pausePlayerTimerForMatchPauseUseCase
            )
    }

    @Test
    fun `invoke should pause match timer and all running player timers`() =
        runTest {
            // Given
            val currentTime = 1000L
            val runningPlayerTimes =
                listOf(
                    PlayerTime(playerId = 1L, isRunning = true, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = 2L, isRunning = true, elapsedTimeMillis = 300L, status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = 3L, isRunning = false, elapsedTimeMillis = 200L),
                )

            coEvery { getAllPlayerTimesUseCase() } returns flowOf(runningPlayerTimes)
            coEvery { pausePlayerTimerForMatchPauseUseCase(any(), any()) } returns Unit

            // When
            pauseMatchUseCase.invoke(currentTime)

            // Then
            coVerify { pauseMatchTimerUseCase(currentTime) }
            coVerify { pausePlayerTimerForMatchPauseUseCase(1L, currentTime) }
            coVerify { pausePlayerTimerForMatchPauseUseCase(2L, currentTime) }
            coVerify(exactly = 0) { pausePlayerTimerForMatchPauseUseCase(3L, any()) }
        }

    @Test
    fun `invoke should pause match timer even when no player timers are running`() =
        runTest {
            // Given
            val currentTime = 1000L
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, isRunning = false, elapsedTimeMillis = 500L),
                    PlayerTime(playerId = 2L, isRunning = false, elapsedTimeMillis = 300L),
                )

            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            // When
            pauseMatchUseCase.invoke(currentTime)

            // Then
            coVerify { pauseMatchTimerUseCase(currentTime) }
            coVerify(exactly = 0) { pausePlayerTimerUseCase(any(), any()) }
        }

    @Test
    fun `invoke should pause match timer when no player times exist`() =
        runTest {
            // Given
            val currentTime = 1000L
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(emptyList())

            // When
            pauseMatchUseCase.invoke(currentTime)

            // Then
            coVerify { pauseMatchTimerUseCase(currentTime) }
            coVerify(exactly = 0) { pausePlayerTimerUseCase(any(), any()) }
        }
}
