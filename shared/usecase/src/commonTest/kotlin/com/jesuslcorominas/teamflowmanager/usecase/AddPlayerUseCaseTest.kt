package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddPlayerUseCaseTest {

    private lateinit var playerRepository: PlayerRepository
    private lateinit var addPlayerUseCase: AddPlayerUseCase

    @Before
    fun setup() {
        playerRepository = mockk(relaxed = true)
        addPlayerUseCase = AddPlayerUseCaseImpl(playerRepository)
    }

    @Test
    fun `invoke should add player to repository`() = runTest {
        // Given
        val player = Player(
            id = 0,
            firstName = "John",
            lastName = "Doe",
            number = 10,
            positions = listOf(Position.Forward)
        )

        // When
        addPlayerUseCase.invoke(player)

        // Then
        coVerify { playerRepository.addPlayer(player) }
    }
}
