package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import io.mockk.coVerify
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import io.mockk.mockk
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import kotlinx.coroutines.test.runTest
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import org.junit.Before
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import org.junit.Test
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner

class PauseMatchTimerUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var transactionRunner: TransactionRunner
    private lateinit var pauseMatchTimerUseCase: PauseMatchTimerUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        transactionRunner = mockk(relaxed = true)
        // Make transactionRunner execute blocks immediately
        coEvery { transactionRunner.run<Unit>(any()) } answers {
            val block = firstArg<suspend () -> Unit>()
            block.invoke()
        }
        pauseMatchTimerUseCase = PauseMatchTimerUseCaseImpl(matchRepository, transactionRunner)
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
