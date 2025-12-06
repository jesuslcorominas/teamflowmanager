package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HasLocalDataWithoutUserIdUseCaseTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var hasLocalDataWithoutUserIdUseCase: HasLocalDataWithoutUserIdUseCase

    @Before
    fun setup() {
        teamRepository = mockk(relaxed = true)
        hasLocalDataWithoutUserIdUseCase = HasLocalDataWithoutUserIdUseCaseImpl(teamRepository)
    }

    @Test
    fun `invoke should return true when local team exists without coachId`() =
        runTest {
            // Given
            coEvery { teamRepository.hasLocalTeamWithoutUserId() } returns true

            // When
            val result = hasLocalDataWithoutUserIdUseCase()

            // Then
            assertTrue(result)
            coVerify { teamRepository.hasLocalTeamWithoutUserId() }
        }

    @Test
    fun `invoke should return false when no local team exists without coachId`() =
        runTest {
            // Given
            coEvery { teamRepository.hasLocalTeamWithoutUserId() } returns false

            // When
            val result = hasLocalDataWithoutUserIdUseCase()

            // Then
            assertFalse(result)
            coVerify { teamRepository.hasLocalTeamWithoutUserId() }
        }

    @Test
    fun `invoke should return false when team has coachId`() =
        runTest {
            // Given
            coEvery { teamRepository.hasLocalTeamWithoutUserId() } returns false

            // When
            val result = hasLocalDataWithoutUserIdUseCase()

            // Then
            assertFalse(result)
            coVerify { teamRepository.hasLocalTeamWithoutUserId() }
        }
}
