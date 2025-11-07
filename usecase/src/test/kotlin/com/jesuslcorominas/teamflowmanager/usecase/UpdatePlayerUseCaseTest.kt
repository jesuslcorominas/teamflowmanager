package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdatePlayerUseCaseTest {

    private lateinit var playerRepository: PlayerRepository
    private lateinit var updatePlayerUseCase: UpdatePlayerUseCase

    @Before
    fun setup() {
        playerRepository = mockk()
        updatePlayerUseCase = UpdatePlayerUseCaseImpl(playerRepository)
    }

    @Test
    fun `invoke should call repository updatePlayer`() = runTest {
        // Given
        val player = Player(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            number = 10,
            positions = listOf(Position.Forward),
            teamId = 1L,
            isCaptain = false
        )
        coEvery { playerRepository.updatePlayer(player) } just runs

        // When
        updatePlayerUseCase.invoke(player)

        // Then
        coVerify { playerRepository.updatePlayer(player) }
    }

    @Test
    fun `invoke should update player with multiple positions`() = runTest {
        // Given
        val player = Player(
            id = 2,
            firstName = "Jane",
            lastName = "Smith",
            number = 8,
            positions = listOf(Position.Midfielder, Position.Defender),
            teamId = 1L,
            isCaptain = false
        )
        coEvery { playerRepository.updatePlayer(player) } just runs

        // When
        updatePlayerUseCase.invoke(player)

        // Then
        coVerify { playerRepository.updatePlayer(player) }
    }
}
