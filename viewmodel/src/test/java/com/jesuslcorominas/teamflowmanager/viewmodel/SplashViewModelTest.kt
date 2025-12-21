package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
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
    private lateinit var getUserClubMembershipUseCase: GetUserClubMembershipUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamUseCase = mockk()
        getCurrentUserUseCase = mockk()
        synchronizeTimeUseCase = mockk()
        getUserClubMembershipUseCase = mockk()
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
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase, getUserClubMembershipUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)
    }

    @Test
    fun `should emit NoClub when user is authenticated but has no club membership`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getUserClubMembershipUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase, getUserClubMembershipUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
        coVerify(exactly = 0) { getTeamUseCase() }
    }

    @Test
    fun `should emit NoTeam when user is authenticated, has club membership, but no team exists`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        val clubMember = ClubMember(
            id = 1L,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100L,
            role = "member",
            firestoreId = "member1"
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)
        every { getTeamUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase, getUserClubMembershipUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.NoTeam, viewModel.uiState.value)
    }

    @Test
    fun `should emit TeamExists when user is authenticated, has club membership, and team exists`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        val clubMember = ClubMember(
            id = 1L,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100L,
            role = "member",
            firestoreId = "member1"
        )
        val team = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)
        every { getTeamUseCase() } returns flowOf(team)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase, getUserClubMembershipUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.TeamExists, viewModel.uiState.value)
    }

    @Test
    fun `should synchronize time on startup`() = runTest {
        // Given
        every { getCurrentUserUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase, getUserClubMembershipUseCase)
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
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, synchronizeTimeUseCase, getUserClubMembershipUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)
        coVerify { synchronizeTimeUseCase() }
    }
}
