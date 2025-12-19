package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
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
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var resumeMatchUseCase: ResumeMatchUseCase

    @Before
    fun setup() {
        startMatchTimerUseCase = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        resumeMatchUseCase =
            ResumeMatchUseCaseImpl(
                startMatchTimerUseCase,
                getAllPlayerTimesUseCase,
                playerTimeRepository,
            )
    }

    @Test
    fun `invoke should resume all paused player timers in batch and then resume match timer`() =
        runTest {
            // Given
            val currentTime = 1000L
            val matchId = 1L
            val pausedPlayerTimes =
                listOf(
                    PlayerTime(playerId = 1L, isRunning = false, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PAUSED),
                    PlayerTime(playerId = 2L, isRunning = false, elapsedTimeMillis = 300L, status = PlayerTimeStatus.PAUSED),
                    PlayerTime(playerId = 3L, isRunning = false, elapsedTimeMillis = 0L, status = PlayerTimeStatus.ON_BENCH),
                )

            coEvery { getAllPlayerTimesUseCase() } returns flowOf(pausedPlayerTimes)

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify { playerTimeRepository.startTimersBatch(listOf(1L, 2L), currentTime) }
            coVerify { startMatchTimerUseCase(matchId, currentTime) }
        }

    @Test
    fun `invoke should resume match timer even when no player times have elapsed time`() =
        runTest {
            // Given
            val currentTime = 1000L
            val matchId = 1L
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, isRunning = false, elapsedTimeMillis = 0L),
                    PlayerTime(playerId = 2L, isRunning = false, elapsedTimeMillis = 0L),
                )

            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatch(any(), any()) }
            coVerify { startMatchTimerUseCase(matchId, currentTime) }
        }

    @Test
    fun `invoke should resume match timer when no player times exist`() =
        runTest {
            // Given
            val currentTime = 1000L
            val matchId = 1L
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(emptyList())

            // When
            resumeMatchUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatch(any(), any()) }
            coVerify { startMatchTimerUseCase(matchId, currentTime) }
        }
}
