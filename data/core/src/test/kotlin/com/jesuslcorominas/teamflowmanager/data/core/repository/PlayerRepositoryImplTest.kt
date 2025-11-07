package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlayerRepositoryImplTest {

    private lateinit var localDataSource: PlayerLocalDataSource
    private lateinit var repository: PlayerRepositoryImpl

    @Before
    fun setup() {
        localDataSource = mockk(relaxed = true)
        repository = PlayerRepositoryImpl(localDataSource)
    }

    @Test
    fun `getAllPlayers should return players from local data source`() = runTest {
        // Given
        val players = listOf(
            Player(1, "John", "Doe", 10, listOf(Position.Forward), teamId = 1L, isCaptain = false),
            Player(2, "Jane", "Smith", 8, listOf(Position.Midfielder), teamId = 1L, isCaptain = false)
        )
        every { localDataSource.getAllPlayers() } returns flowOf(players)

        // When
        val result = repository.getAllPlayers().first()

        // Then
        assertEquals(players, result)
        verify { localDataSource.getAllPlayers() }
    }

    @Test
    fun `getAllPlayers should return empty list when no players exist`() = runTest {
        // Given
        every { localDataSource.getAllPlayers() } returns flowOf(emptyList())

        // When
        val result = repository.getAllPlayers().first()

        // Then
        assertEquals(emptyList<Player>(), result)
        verify { localDataSource.getAllPlayers() }
    }

    @Test
    fun `addPlayer should call insertPlayer on local data source`() = runTest {
        // Given
        val player = Player(
            id = 0,
            firstName = "John",
            lastName = "Doe",
            number = 10,
            positions = listOf(Position.Forward),
            teamId = 1L,
            isCaptain = false
        )

        // When
        repository.addPlayer(player)

        // Then
        coVerify { localDataSource.insertPlayer(player) }
    }

    @Test
    fun `deletePlayer should delete player from local data source`() = runTest {
        // Given
        val playerId = 1L
        coEvery { localDataSource.deletePlayer(playerId) } just runs

        // When
        repository.deletePlayer(playerId)

        // Then
        coVerify { localDataSource.deletePlayer(playerId) }
    }

    @Test
    fun `updatePlayer should call local data source updatePlayer`() = runTest {
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
        coEvery { localDataSource.updatePlayer(player) } just runs

        // When
        repository.updatePlayer(player)

        // Then
        coVerify { localDataSource.updatePlayer(player) }
    }
}
