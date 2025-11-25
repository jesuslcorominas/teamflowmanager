package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PausePlayerTimerUseCaseTest {
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var pausePlayerTimerUseCase: PausePlayerTimerUseCase

    @Before
    fun setup() {
        playerTimeRepository = mockk(relaxed = true)
        pausePlayerTimerUseCase = PausePlayerTimerUseCaseImpl(playerTimeRepository)
    }

    @Test
    fun `invoke should call pauseTimer on repository with player id and current time`() =
        runTest {
            // Given
            val playerId = 1L
            val currentTime = 3000L

            // When
            pausePlayerTimerUseCase.invoke(playerId, currentTime)

            // Then
            coVerify { playerTimeRepository.pauseTimer(playerId, currentTime) }
        }
}
