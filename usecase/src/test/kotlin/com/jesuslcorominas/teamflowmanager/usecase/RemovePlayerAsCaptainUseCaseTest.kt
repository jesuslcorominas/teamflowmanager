package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RemovePlayerAsCaptainUseCaseTest {
    private lateinit var playerRepository: PlayerRepository
    private lateinit var useCase: RemovePlayerAsCaptainUseCase

    @Before
    fun setup() {
        playerRepository = mockk(relaxed = true)
        useCase = RemovePlayerAsCaptainUseCaseImpl(playerRepository)
    }

    @Test
    fun `invoke should call removePlayerAsCaptain on repository`() = runTest {
        val playerId = 5L

        useCase.invoke(playerId)

        coVerify { playerRepository.removePlayerAsCaptain(playerId) }
    }
}
