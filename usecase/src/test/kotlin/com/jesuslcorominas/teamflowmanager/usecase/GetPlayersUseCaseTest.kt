package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetPlayersUseCaseTest {

    private lateinit var playerRepository: PlayerRepository
    private lateinit var getPlayersUseCase: GetPlayersUseCase

    @Before
    fun setup() {
        playerRepository = mockk()
        getPlayersUseCase = GetPlayersUseCaseImpl(playerRepository)
    }

    @Test
    fun `invoke should return players from repository`() = runTest {
        // Given
        val players = listOf(
            Player(1, "John", "Doe", LocalDate.of(2010, 5, 15), listOf(Position.Forward)),
            Player(2, "Jane", "Smith", LocalDate.of(2011, 3, 20), listOf(Position.Midfielder))
        )
        every { playerRepository.getAllPlayers() } returns flowOf(players)

        // When
        val result = getPlayersUseCase.invoke().first()

        // Then
        assertEquals(players, result)
        verify { playerRepository.getAllPlayers() }
    }

    @Test
    fun `invoke should return empty list when no players exist`() = runTest {
        // Given
        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())

        // When
        val result = getPlayersUseCase.invoke().first()

        // Then
        assertEquals(emptyList<Player>(), result)
        verify { playerRepository.getAllPlayers() }
    }
}
