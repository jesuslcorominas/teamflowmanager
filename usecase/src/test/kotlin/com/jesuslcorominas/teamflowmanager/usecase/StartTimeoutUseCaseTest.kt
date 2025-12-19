package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StartTimeoutUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var startTimeoutUseCase: StartTimeoutUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        startTimeoutUseCase =
            StartTimeoutUseCaseImpl(
                matchRepository,
                getAllPlayerTimesUseCase,
                playerTimeRepository
            )
    }

    @Test
    fun `invoke should pause all running player timers in batch and then start timeout timer`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 1000L
            val runningPlayerTimes =
                listOf(
                    PlayerTime(playerId = 1L, isRunning = true, elapsedTimeMillis = 500L, status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = 2L, isRunning = true, elapsedTimeMillis = 300L, status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = 3L, isRunning = false, elapsedTimeMillis = 200L),
                )

            coEvery { getAllPlayerTimesUseCase() } returns flowOf(runningPlayerTimes)

            // When
            startTimeoutUseCase.invoke(matchId, currentTime)

            // Then
            coVerify { playerTimeRepository.pauseTimersBatch(listOf(1L, 2L), currentTime) }
            coVerify { matchRepository.startTimeout(matchId, currentTime) }
        }

    @Test
    fun `invoke should start timeout timer even when no player timers are running`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 1000L
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, isRunning = false, elapsedTimeMillis = 500L),
                    PlayerTime(playerId = 2L, isRunning = false, elapsedTimeMillis = 300L),
                )

            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            // When
            startTimeoutUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { playerTimeRepository.pauseTimersBatch(any(), any()) }
            coVerify { matchRepository.startTimeout(matchId, currentTime) }
        }

    @Test
    fun `invoke should start timeout timer when no player times exist`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 1000L
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(emptyList())

            // When
            startTimeoutUseCase.invoke(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { playerTimeRepository.pauseTimersBatch(any(), any()) }
            coVerify { matchRepository.startTimeout(matchId, currentTime) }
        }
}
