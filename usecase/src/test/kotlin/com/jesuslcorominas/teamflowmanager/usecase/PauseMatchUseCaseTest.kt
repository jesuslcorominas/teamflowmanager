package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import io.mockk.coEvery
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import io.mockk.coVerify
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import io.mockk.mockk
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import kotlinx.coroutines.flow.flowOf
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import kotlinx.coroutines.test.runTest
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import org.junit.Before
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import org.junit.Test
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner

class PauseMatchUseCaseTest {
    private lateinit var pauseMatchTimerUseCase: PauseMatchTimerUseCase
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var pausePlayerTimerUseCase: PausePlayerTimerUseCase
    private lateinit var pauseMatchUseCase: PauseMatchUseCase
    private lateinit var transactionRunner: TransactionRunner

    private lateinit var pausePlayerTimerForMatchPauseUseCase: PausePlayerTimerForMatchPauseUseCase

    @Before
    fun setup() {
        pauseMatchTimerUseCase = mockk(relaxed = true)
        transactionRunner = mockk(relaxed = true)
        // Make transactionRunner execute blocks immediately
        coEvery { transactionRunner.run<Unit>(any()) } answers {
            val block = firstArg<suspend () -> Unit>()
            block.invoke()
        }
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        transactionRunner = mockk(relaxed = true)
        // Make transactionRunner execute blocks immediately
        coEvery { transactionRunner.run<Unit>(any()) } answers {
            val block = firstArg<suspend () -> Unit>()
            block.invoke()
        }
        pausePlayerTimerUseCase = mockk(relaxed = true)
        transactionRunner = mockk(relaxed = true)
        // Make transactionRunner execute blocks immediately
        coEvery { transactionRunner.run<Unit>(any()) } answers {
            val block = firstArg<suspend () -> Unit>()
            block.invoke()
        }
        pausePlayerTimerForMatchPauseUseCase = mockk(relaxed = true)
        transactionRunner = mockk(relaxed = true)
        // Make transactionRunner execute blocks immediately
        coEvery { transactionRunner.run<Unit>(any()) } answers {
            val block = firstArg<suspend () -> Unit>()
            block.invoke()
        }
        pauseMatchUseCase =
            PauseMatchUseCaseImpl(
                pauseMatchTimerUseCase,
                getAllPlayerTimesUseCase,
                pausePlayerTimerForMatchPauseUseCase
            , transactionRunner)
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
