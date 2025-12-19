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

class EndTimeoutUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var endTimeoutUseCase: EndTimeoutUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        endTimeoutUseCase =
            EndTimeoutUseCaseImpl(
                matchRepository,
                getAllPlayerTimesUseCase,
                playerTimeRepository
            )
    }

    @Test
    fun `invoke should resume all paused player timers in batch and then end timeout timer`() =
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

            // When
            endTimeoutUseCase.invoke(matchId, currentTime)

            // Then
            coVerify { playerTimeRepository.startTimersBatch(listOf(1L, 2L), currentTime) }
            coVerify { matchRepository.endTimeout(matchId, currentTime) }
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
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatch(any(), any()) }
            coVerify { matchRepository.endTimeout(matchId, currentTime) }
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
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatch(any(), any()) }
            coVerify { matchRepository.endTimeout(matchId, currentTime) }
        }
}
