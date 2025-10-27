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

class EndTimeoutUseCaseTest {
    private lateinit var endTimeoutTimerUseCase: EndTimeoutTimerUseCase
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var startPlayerTimerUseCase: StartPlayerTimerUseCase
    private lateinit var endTimeoutUseCase: EndTimeoutUseCase

    @Before
    fun setup() {
        endTimeoutTimerUseCase = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        startPlayerTimerUseCase = mockk(relaxed = true)
        endTimeoutUseCase =
            EndTimeoutUseCaseImpl(
                endTimeoutTimerUseCase,
                getAllPlayerTimesUseCase,
                startPlayerTimerUseCase
            )
    }

    @Test
    fun `invoke should end timeout timer and resume all paused player timers`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 2000L
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, isRunning = false, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PAUSED),
                    PlayerTime(playerId = 2L, isRunning = false, elapsedTimeMillis = 300L, status = PlayerTimeStatus.PAUSED),
                    PlayerTime(playerId = 3L, isRunning = false, elapsedTimeMillis = 200L, status = PlayerTimeStatus.ON_BENCH),
                )

            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)
            coEvery { startPlayerTimerUseCase(any(), any()) } returns Unit

            // When
            endTimeoutUseCase.invoke(matchId, currentTime)

            // Then
            coVerify { endTimeoutTimerUseCase(matchId, currentTime) }
            coVerify { startPlayerTimerUseCase(1L, currentTime) }
            coVerify { startPlayerTimerUseCase(2L, currentTime) }
            coVerify(exactly = 0) { startPlayerTimerUseCase(3L, any()) }
        }

    @Test
    fun `invoke should end timeout timer even when no player timers are paused`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 2000L
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, isRunning = false, elapsedTimeMillis = 500L, status = PlayerTimeStatus.ON_BENCH),
                    PlayerTime(playerId = 2L, isRunning = true, elapsedTimeMillis = 300L, status = PlayerTimeStatus.PLAYING),
                )

            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            // When
            endTimeoutUseCase.invoke(matchId, currentTime)

            // Then
            coVerify { endTimeoutTimerUseCase(matchId, currentTime) }
            coVerify(exactly = 0) { startPlayerTimerUseCase(any(), any()) }
        }

    @Test
    fun `invoke should end timeout timer when no player times exist`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 2000L
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(emptyList())

            // When
            endTimeoutUseCase.invoke(matchId, currentTime)

            // Then
            coVerify { endTimeoutTimerUseCase(matchId, currentTime) }
            coVerify(exactly = 0) { startPlayerTimerUseCase(any(), any()) }
        }
}
