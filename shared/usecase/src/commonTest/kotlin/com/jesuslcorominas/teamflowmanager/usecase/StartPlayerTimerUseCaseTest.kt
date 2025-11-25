package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StartPlayerTimerUseCaseTest {
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var startPlayerTimerUseCase: StartPlayerTimerUseCase

    @Before
    fun setup() {
        playerTimeRepository = mockk(relaxed = true)
        startPlayerTimerUseCase = StartPlayerTimerUseCaseImpl(playerTimeRepository)
    }

    @Test
    fun `invoke should call startTimer on repository with player id and current time`() =
        runTest {
            // Given
            val playerId = 1L
            val currentTime = 1000L

            // When
            startPlayerTimerUseCase.invoke(playerId, currentTime)

            // Then
            coVerify { playerTimeRepository.startTimer(playerId, currentTime) }
        }
}
