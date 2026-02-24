package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.PausePlayerTimerForMatchPauseUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PausePlayerTimerForMatchPauseUseCaseTest {
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var useCase: PausePlayerTimerForMatchPauseUseCase

    @Before
    fun setup() {
        playerTimeRepository = mockk(relaxed = true)
        useCase = PausePlayerTimerForMatchPauseUseCaseImpl(playerTimeRepository)
    }

    @Test
    fun `invoke should call pauseTimerForMatchPause on repository`() = runTest {
        val playerId = 3L
        val currentTimeMillis = 5000L

        useCase.invoke(playerId, currentTimeMillis)

        coVerify { playerTimeRepository.pauseTimerForMatchPause(playerId, currentTimeMillis) }
    }
}
