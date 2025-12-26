package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
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
    private lateinit var synchronizeTimeUseCase: SynchronizeTimeUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamUseCase = mockk()
        getCurrentUserUseCase = mockk()
        synchronizeTimeUseCase = mockk()
        coEvery { synchronizeTimeUseCase() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should emit NotAuthenticated when user is not logged in`() = runTest {
        // Given
        every { getCurrentUserUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)
    }

    @Test
    fun `should emit NoClub when user is authenticated but has no team`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getTeamUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    @Test
    fun `should emit NoClub when user has team but team has no club`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        val teamWithoutClub = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = null,
            clubFirestoreId = null
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getTeamUseCase() } returns flowOf(teamWithoutClub)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    @Test
    fun `should emit TeamExists when user has team and team has club (clubId)`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        val teamWithClub = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = 100L,
            clubFirestoreId = null
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.TeamExists, viewModel.uiState.value)
    }

    @Test
    fun `should emit TeamExists when user has team and team has club (clubFirestoreId)`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        val teamWithClub = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = null,
            clubFirestoreId = "club123"
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.TeamExists, viewModel.uiState.value)
    }

    @Test
    fun `should synchronize time on startup`() = runTest {
        // Given
        every { getCurrentUserUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase)
        advanceUntilIdle()

        // Then
        coVerify { synchronizeTimeUseCase() }
    }

    @Test
    fun `should continue with authentication check even if time sync fails`() = runTest {
        // Given
        coEvery { synchronizeTimeUseCase() } throws Exception("Test exception")
        every { getCurrentUserUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)
        coVerify { synchronizeTimeUseCase() }
    }
}
