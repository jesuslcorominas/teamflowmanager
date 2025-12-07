package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.HasLocalDataWithoutUserIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getTeamUseCase: GetTeamUseCase
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var hasLocalDataWithoutUserIdUseCase: HasLocalDataWithoutUserIdUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamUseCase = mockk()
        getCurrentUserUseCase = mockk()
        hasLocalDataWithoutUserIdUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should emit NotAuthenticated when user is not logged in`() = runTest {
        // Given
        coEvery { hasLocalDataWithoutUserIdUseCase() } returns false
        every { getCurrentUserUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, hasLocalDataWithoutUserIdUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)
        coVerify { hasLocalDataWithoutUserIdUseCase() }
    }

    @Test
    fun `should emit NoTeam when user is authenticated but no team exists`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        coEvery { hasLocalDataWithoutUserIdUseCase() } returns false
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getTeamUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, hasLocalDataWithoutUserIdUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.NoTeam, viewModel.uiState.value)
        coVerify { hasLocalDataWithoutUserIdUseCase() }
    }

    @Test
    fun `should emit TeamExists when user is authenticated and team exists`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        val team = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5
        )
        coEvery { hasLocalDataWithoutUserIdUseCase() } returns false
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getTeamUseCase() } returns flowOf(team)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, hasLocalDataWithoutUserIdUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.TeamExists, viewModel.uiState.value)
        coVerify { hasLocalDataWithoutUserIdUseCase() }
    }

    @Test
    fun `should check for local data without user ID at startup`() = runTest {
        // Given
        coEvery { hasLocalDataWithoutUserIdUseCase() } returns true
        every { getCurrentUserUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, hasLocalDataWithoutUserIdUseCase)
        advanceUntilIdle()

        // Then
        coVerify { hasLocalDataWithoutUserIdUseCase() }
    }

    @Test
    fun `should continue with authentication check even if local data check fails`() = runTest {
        // Given
        coEvery { hasLocalDataWithoutUserIdUseCase() } throws Exception("Test exception")
        every { getCurrentUserUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, hasLocalDataWithoutUserIdUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)
        coVerify { hasLocalDataWithoutUserIdUseCase() }
    }
}
