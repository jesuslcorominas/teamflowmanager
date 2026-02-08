package com.jesuslcorominas.teamflowmanager.viewmodel

import android.util.Log
import com.jesuslcorominas.teamflowmanager.domain.model.*
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var getTeamUseCase: GetTeamUseCase
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var getUserClubMembershipUseCase: GetUserClubMembershipUseCase
    private lateinit var synchronizeTimeUseCase: SynchronizeTimeUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>(), any<Throwable>()) } returns 0

        getTeamUseCase = mockk()
        getCurrentUserUseCase = mockk()
        getUserClubMembershipUseCase = mockk()
        synchronizeTimeUseCase = mockk()

        every { getTeamUseCase() } returns flowOf(null)
        every { getCurrentUserUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
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
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, getUserClubMembershipUseCase, synchronizeTimeUseCase)

        // Then
        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)
    }

    @Test
    fun `should emit NoClub when user is authenticated but has no team and no club membership`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, getUserClubMembershipUseCase, synchronizeTimeUseCase)

        // Then
        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    @Test
    fun `should emit ClubPresident when user has no team but is a President`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        val clubMember = ClubMember(
            id = 1,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100,
            role = "Presidente",
            firestoreId = "clubmember_doc_123",
            clubFirestoreId = "club123"
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, getUserClubMembershipUseCase, synchronizeTimeUseCase)

        // Then
        assertEquals(SplashViewModel.UiState.ClubPresident, viewModel.uiState.value)
    }

    @Test
    fun `should emit NoClub when user has no team and is a club member but not President`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        val clubMember = ClubMember(
            id = 1,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100,
            role = "Coach",
            firestoreId = "clubmember_doc_123",
            clubFirestoreId = "club123"
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, getUserClubMembershipUseCase, synchronizeTimeUseCase)

        // Then
        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    @Test
    fun `should emit ClubPresident when user is President even if they own a team`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        val clubMember = ClubMember(
            id = 1,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100,
            role = "Presidente",
            firestoreId = "clubmember_doc_123",
            clubFirestoreId = "club123"
        )
        val teamWithClub = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            captainId = null,
            teamType = TeamType.FOOTBALL_5,
            coachId = null,
            clubId = 100L,
            clubFirestoreId = "club123"
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, getUserClubMembershipUseCase, synchronizeTimeUseCase)

        // Then
        assertEquals(SplashViewModel.UiState.ClubPresident, viewModel.uiState.value)
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
            captainId = null,
            teamType = TeamType.FOOTBALL_5,
            coachId = null,
            clubId = null,
            clubFirestoreId = null
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        every { getTeamUseCase() } returns flowOf(teamWithoutClub)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, getUserClubMembershipUseCase, synchronizeTimeUseCase)

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
        val clubMember = ClubMember(
            id = 1,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100,
            role = "Coach",
            firestoreId = "clubmember_doc_123",
            clubFirestoreId = "club123"
        )
        val teamWithClub = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            captainId = null,
            teamType = TeamType.FOOTBALL_5,
            coachId = null,
            clubId = 100L,
            clubFirestoreId = null
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, getUserClubMembershipUseCase, synchronizeTimeUseCase)

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
        val clubMember = ClubMember(
            id = 1,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100,
            role = "Coach",
            firestoreId = "clubmember_doc_123",
            clubFirestoreId = "club123"
        )
        val teamWithClub = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            captainId = null,
            teamType = TeamType.FOOTBALL_5,
            coachId = null,
            clubId = null,
            clubFirestoreId = "club123"
        )
        every { getCurrentUserUseCase() } returns flowOf(user)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, getUserClubMembershipUseCase, synchronizeTimeUseCase)

        // Then
        assertEquals(SplashViewModel.UiState.TeamExists, viewModel.uiState.value)
    }

    @Test
    fun `should synchronize time on startup`() = runTest {
        // Given
        every { getCurrentUserUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, getUserClubMembershipUseCase, synchronizeTimeUseCase)

        // Then
        coVerify { synchronizeTimeUseCase() }
    }

    @Test
    fun `should continue with authentication check even if time sync fails`() = runTest {
        // Given
        coEvery { synchronizeTimeUseCase() } throws Exception("Test exception")
        every { getCurrentUserUseCase() } returns flowOf(null)

        // When
        val viewModel = SplashViewModel(getTeamUseCase, getCurrentUserUseCase, getUserClubMembershipUseCase, synchronizeTimeUseCase)

        // Then
        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)
        coVerify { synchronizeTimeUseCase() }
    }
}
