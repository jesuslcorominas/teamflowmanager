package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeletePlayerUseCaseTest {

    private lateinit var playerRepository: PlayerRepository
    private lateinit var deletePlayerUseCase: DeletePlayerUseCase

    @Before
    fun setup() {
        playerRepository = mockk()
        deletePlayerUseCase = DeletePlayerUseCaseImpl(playerRepository)
    }

    @Test
    fun `invoke should delete player from repository`() = runTest {
        // Given
        val playerId = 1L
        coEvery { playerRepository.deletePlayer(playerId) } just runs

        // When
        deletePlayerUseCase.invoke(playerId)

        // Then
        coVerify { playerRepository.deletePlayer(playerId) }
    }
}
