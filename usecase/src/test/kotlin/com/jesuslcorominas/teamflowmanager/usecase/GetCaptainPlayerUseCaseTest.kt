package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetCaptainPlayerUseCaseTest {
    private lateinit var playerRepository: PlayerRepository
    private lateinit var useCase: GetCaptainPlayerUseCase

    @Before
    fun setup() {
        playerRepository = mockk()
        useCase = GetCaptainPlayerUseCaseImpl(playerRepository)
    }

    @Test
    fun `invoke should return captain player when exists`() = runTest {
        // Given
        val captain = Player(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            number = 10,
            positions = listOf(Position.Forward, teamId = 1L, isCaptain = false),
            isCaptain = true
        )
        coEvery { playerRepository.getCaptainPlayer() } returns captain

        // When
        val result = useCase.invoke()

        // Then
        assertEquals(captain, result)
    }

    @Test
    fun `invoke should return null when no captain exists`() = runTest {
        // Given
        coEvery { playerRepository.getCaptainPlayer() } returns null

        // When
        val result = useCase.invoke()

        // Then
        assertNull(result)
    }
}
