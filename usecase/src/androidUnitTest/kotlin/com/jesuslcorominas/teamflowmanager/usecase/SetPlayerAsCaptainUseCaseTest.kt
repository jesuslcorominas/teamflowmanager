package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SetPlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SetPlayerAsCaptainUseCaseTest {
    private lateinit var playerRepository: PlayerRepository
    private lateinit var useCase: SetPlayerAsCaptainUseCase

    @Before
    fun setup() {
        playerRepository = mockk(relaxed = true)
        useCase = SetPlayerAsCaptainUseCaseImpl(playerRepository)
    }

    @Test
    fun `invoke should call setPlayerAsCaptain on repository`() = runTest {
        val playerId = 5L

        useCase.invoke(playerId)

        coVerify { playerRepository.setPlayerAsCaptain(playerId) }
    }
}
