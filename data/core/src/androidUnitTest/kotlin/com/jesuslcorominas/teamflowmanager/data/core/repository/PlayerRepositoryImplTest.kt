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
        id: String = "1",
        firstName: String = "John",
        lastName: String = "Doe",
        number: Int = 10,
        positions: List<Position> = listOf(Position.Forward),
        teamId: String = "1",
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
            createPlayer(id = "1", firstName = "John"),
            createPlayer(id = "2", firstName = "Jane", positions = listOf(Position.Midfielder)),
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
        val player = createPlayer(id = "1")
        coEvery { playerDataSource.getPlayerById("1") } returns player

        val result = repository.getPlayerById("1")

        assertEquals(player, result)
    }

    @Test
    fun `givenUnknownPlayerId_whenGetPlayerById_thenReturnsNull`() = runTest {
        coEvery { playerDataSource.getPlayerById("99") } returns null

        val result = repository.getPlayerById("99")

        assertNull(result)
    }

    // --- getCaptainPlayer ---

    @Test
    fun `givenCaptainExists_whenGetCaptainPlayer_thenReturnsCaptain`() = runTest {
        val captain = createPlayer(id = "3", isCaptain = true)
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
        val player = createPlayer(id = "")
        coEvery { playerDataSource.insertPlayer(player) } returns "1"

        val result = repository.addPlayer(player)

        assertEquals("1", result)
        coVerify { playerDataSource.insertPlayer(player) }
    }

    // --- deletePlayer ---

    @Test
    fun `givenPlayerId_whenDeletePlayer_thenDelegatesToDataSource`() = runTest {
        coEvery { playerDataSource.deletePlayer("1") } just runs

        repository.deletePlayer("1")

        coVerify { playerDataSource.deletePlayer("1") }
    }

    // --- updatePlayer ---

    @Test
    fun `givenPlayer_whenUpdatePlayer_thenDelegatesToDataSource`() = runTest {
        val player = createPlayer(id = "1", firstName = "Updated")
        coEvery { playerDataSource.updatePlayer(player) } just runs

        repository.updatePlayer(player)

        coVerify { playerDataSource.updatePlayer(player) }
    }

    // --- setPlayerAsCaptain ---

    @Test
    fun `givenPlayerId_whenSetPlayerAsCaptain_thenDelegatesToDataSource`() = runTest {
        repository.setPlayerAsCaptain("1")

        coVerify { playerDataSource.setPlayerAsCaptain("1") }
    }

    // --- removePlayerAsCaptain ---

    @Test
    fun `givenPlayerId_whenRemovePlayerAsCaptain_thenDelegatesToDataSource`() = runTest {
        repository.removePlayerAsCaptain("1")

        coVerify { playerDataSource.removePlayerAsCaptain("1") }
    }
}
