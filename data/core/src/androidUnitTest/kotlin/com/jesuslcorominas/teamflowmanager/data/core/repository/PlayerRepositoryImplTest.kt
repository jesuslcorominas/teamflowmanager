package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PlayerRepositoryImplTest {

    private lateinit var playerDataSource: PlayerDataSource
    private lateinit var repository: PlayerRepositoryImpl

    @Before
    fun setup() {
        playerDataSource = mockk(relaxed = true)
        repository = PlayerRepositoryImpl(playerDataSource)
    }

    private fun createPlayer(
        id: Long = 1L,
        firstName: String = "John",
        lastName: String = "Doe",
        number: Int = 10,
        positions: List<Position> = listOf(Position.Forward),
        teamId: Long = 1L,
        isCaptain: Boolean = false,
    ) = Player(
        id = id,
        firstName = firstName,
        lastName = lastName,
        number = number,
        positions = positions,
        teamId = teamId,
        isCaptain = isCaptain,
    )

    // --- getAllPlayers ---

    @Test
    fun `givenPlayers_whenGetAllPlayers_thenDelegatesToDataSource`() = runTest {
        val players = listOf(
            createPlayer(id = 1L, firstName = "John"),
            createPlayer(id = 2L, firstName = "Jane", positions = listOf(Position.Midfielder)),
        )
        every { playerDataSource.getAllPlayers() } returns flowOf(players)

        val result = repository.getAllPlayers().first()

        assertEquals(players, result)
    }

    @Test
    fun `givenNoPlayers_whenGetAllPlayers_thenReturnsEmptyList`() = runTest {
        every { playerDataSource.getAllPlayers() } returns flowOf(emptyList())

        val result = repository.getAllPlayers().first()

        assertEquals(emptyList<Player>(), result)
    }

    // --- getPlayerById ---

    @Test
    fun `givenExistingPlayerId_whenGetPlayerById_thenReturnsPlayer`() = runTest {
        val player = createPlayer(id = 1L)
        coEvery { playerDataSource.getPlayerById(1L) } returns player

        val result = repository.getPlayerById(1L)

        assertEquals(player, result)
    }

    @Test
    fun `givenUnknownPlayerId_whenGetPlayerById_thenReturnsNull`() = runTest {
        coEvery { playerDataSource.getPlayerById(99L) } returns null

        val result = repository.getPlayerById(99L)

        assertNull(result)
    }

    // --- getCaptainPlayer ---

    @Test
    fun `givenCaptainExists_whenGetCaptainPlayer_thenReturnsCaptain`() = runTest {
        val captain = createPlayer(id = 3L, isCaptain = true)
        coEvery { playerDataSource.getCaptainPlayer() } returns captain

        val result = repository.getCaptainPlayer()

        assertEquals(captain, result)
    }

    @Test
    fun `givenNoCaptainAssigned_whenGetCaptainPlayer_thenReturnsNull`() = runTest {
        coEvery { playerDataSource.getCaptainPlayer() } returns null

        val result = repository.getCaptainPlayer()

        assertNull(result)
    }

    // --- addPlayer ---

    @Test
    fun `givenNewPlayer_whenAddPlayer_thenReturnsInsertedId`() = runTest {
        val player = createPlayer(id = 0L)
        coEvery { playerDataSource.insertPlayer(player) } returns 1L

        val result = repository.addPlayer(player)

        assertEquals(1L, result)
        coVerify { playerDataSource.insertPlayer(player) }
    }

    // --- deletePlayer ---

    @Test
    fun `givenPlayerId_whenDeletePlayer_thenDelegatesToDataSource`() = runTest {
        coEvery { playerDataSource.deletePlayer(1L) } just runs

        repository.deletePlayer(1L)

        coVerify { playerDataSource.deletePlayer(1L) }
    }

    // --- updatePlayer ---

    @Test
    fun `givenPlayer_whenUpdatePlayer_thenDelegatesToDataSource`() = runTest {
        val player = createPlayer(id = 1L, firstName = "Updated")
        coEvery { playerDataSource.updatePlayer(player) } just runs

        repository.updatePlayer(player)

        coVerify { playerDataSource.updatePlayer(player) }
    }

    // --- setPlayerAsCaptain ---

    @Test
    fun `givenPlayerId_whenSetPlayerAsCaptain_thenDelegatesToDataSource`() = runTest {
        repository.setPlayerAsCaptain(1L)

        coVerify { playerDataSource.setPlayerAsCaptain(1L) }
    }

    // --- removePlayerAsCaptain ---

    @Test
    fun `givenPlayerId_whenRemovePlayerAsCaptain_thenDelegatesToDataSource`() = runTest {
        repository.removePlayerAsCaptain(1L)

        coVerify { playerDataSource.removePlayerAsCaptain(1L) }
    }
}
