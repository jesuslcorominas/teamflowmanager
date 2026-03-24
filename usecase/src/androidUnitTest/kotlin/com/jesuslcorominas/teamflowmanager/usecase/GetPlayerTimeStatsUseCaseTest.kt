package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerTimeStatsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetPlayerTimeStatsUseCaseTest {
    private lateinit var playerRepository: PlayerRepository
    private lateinit var playerTimeHistoryRepository: PlayerTimeHistoryRepository
    private lateinit var useCase: GetPlayerTimeStatsUseCase

    @Before
    fun setup() {
        playerRepository = mockk()
        playerTimeHistoryRepository = mockk()
        useCase = GetPlayerTimeStatsUseCaseImpl(playerRepository, playerTimeHistoryRepository)
    }

    @Test
    fun `invoke should return empty list when no players`() = runTest {
        every { playerRepository.getAllPlayers() } returns flowOf(emptyList())
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(emptyList())

        val result = useCase.invoke().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke should return correct time minutes and matches played per player`() = runTest {
        val player1 = createPlayer(1L, 10)
        val player2 = createPlayer(2L, 7)
        val history = listOf(
            PlayerTimeHistory(playerId = 1L, matchId = 1L, elapsedTimeMillis = 60000L, savedAtMillis = 0L),
            PlayerTimeHistory(playerId = 1L, matchId = 2L, elapsedTimeMillis = 120000L, savedAtMillis = 0L),
            PlayerTimeHistory(playerId = 2L, matchId = 1L, elapsedTimeMillis = 90000L, savedAtMillis = 0L),
        )
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(history)

        val result = useCase.invoke().first()

        assertEquals(2, result.size)
        // Sorted descending by total time: player1 (3 min) first
        val stats1 = result.find { it.player.id == 1L }!!
        assertEquals(3.0, stats1.totalTimeMinutes, 0.01) // (60000+120000)/60000
        assertEquals(2, stats1.matchesPlayed)

        val stats2 = result.find { it.player.id == 2L }!!
        assertEquals(1.5, stats2.totalTimeMinutes, 0.01) // 90000/60000
        assertEquals(1, stats2.matchesPlayed)
    }

    @Test
    fun `invoke should return zero stats for player with no history`() = runTest {
        val player = createPlayer(1L, 10)
        every { playerRepository.getAllPlayers() } returns flowOf(listOf(player))
        every { playerTimeHistoryRepository.getAllPlayerTimeHistory() } returns flowOf(emptyList())

        val result = useCase.invoke().first()

        assertEquals(1, result.size)
        assertEquals(0.0, result[0].totalTimeMinutes, 0.01)
        assertEquals(0, result[0].matchesPlayed)
    }

    private fun createPlayer(id: Long, number: Int) = Player(
        id = id,
        firstName = "Player",
        lastName = "Test",
        number = number,
        positions = listOf(Position.Forward),
        teamId = 1L,
        isCaptain = false,
    )
}
