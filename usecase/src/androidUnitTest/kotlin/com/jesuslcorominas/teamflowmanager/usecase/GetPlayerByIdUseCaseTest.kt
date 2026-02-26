package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetPlayerByIdUseCaseTest {
    private lateinit var playerRepository: PlayerRepository
    private lateinit var useCase: GetPlayerByIdUseCase

    @Before
    fun setup() {
        playerRepository = mockk()
        useCase = GetPlayerByIdUseCaseImpl(playerRepository)
    }

    @Test
    fun `invoke should return player when found`() = runTest {
        val playerId = 1L
        val player = Player(
            id = playerId,
            firstName = "John",
            lastName = "Doe",
            number = 10,
            positions = listOf(Position.Forward),
            teamId = 1L,
            isCaptain = false,
        )
        coEvery { playerRepository.getPlayerById(playerId) } returns player

        val result = useCase.invoke(playerId)

        assertEquals(player, result)
    }

    @Test
    fun `invoke should return null when player not found`() = runTest {
        val playerId = 99L
        coEvery { playerRepository.getPlayerById(playerId) } returns null

        val result = useCase.invoke(playerId)

        assertNull(result)
    }
}
