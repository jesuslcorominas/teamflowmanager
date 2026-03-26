package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.IsNotificationPermissionGrantedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SyncFcmTokenUseCase
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
    private lateinit var getUserClubMembershipUseCase: GetUserClubMembershipUseCase
    private lateinit var synchronizeTimeUseCase: SynchronizeTimeUseCase
    private lateinit var syncFcmTokenUseCase: SyncFcmTokenUseCase
    private lateinit var isNotificationPermissionGranted: IsNotificationPermissionGrantedUseCase

    private val testUser = User(
        id = "user123",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamUseCase = mockk()
        getCurrentUserUseCase = mockk()
        getUserClubMembershipUseCase = mockk()
        synchronizeTimeUseCase = mockk()
        syncFcmTokenUseCase = mockk(relaxed = true)
        isNotificationPermissionGranted = mockk()
        coEvery { synchronizeTimeUseCase() } returns Unit
        every { isNotificationPermissionGranted() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SplashViewModel(
        getTeam = getTeamUseCase,
        getCurrentUser = getCurrentUserUseCase,
        getUserClubMembership = getUserClubMembershipUseCase,
        synchronizeTimeUseCase = synchronizeTimeUseCase,
        syncFcmTokenUseCase = syncFcmTokenUseCase,
        isNotificationPermissionGranted = isNotificationPermissionGranted,
    )

    @Test
    fun `should emit NotAuthenticated when user is not logged in`() = runTest {
        every { getCurrentUserUseCase() } returns flowOf(null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)
    }

    @Test
    fun `should emit NoClub when user is authenticated but has no team and no club membership`() = runTest {
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    @Test
    fun `should emit ClubPresident when user has no team but is a President`() = runTest {
        val clubMember = ClubMember(
            id = 1,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100,
            roles = listOf("Presidente"),
            firestoreId = "clubmember_doc_123",
            clubFirestoreId = "club123",
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.ClubPresident, viewModel.uiState.value)
    }

    @Test
    fun `should emit NoClub when user has no team and is a club member but not President`() = runTest {
        val clubMember = ClubMember(
            id = 1,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100,
            roles = listOf("Coach"),
            firestoreId = "clubmember_doc_123",
            clubFirestoreId = "club123",
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    @Test
    fun `should emit ClubPresident when user is President even if they own a team`() = runTest {
        val clubMember = ClubMember(
            id = 1,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100,
            roles = listOf("Presidente"),
            firestoreId = "clubmember_doc_123",
            clubFirestoreId = "club123",
        )
        val teamWithClub = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = 100L,
            clubFirestoreId = "club123",
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.ClubPresident, viewModel.uiState.value)
    }

    @Test
    fun `should emit NoClub when user has team but team has no clubFirestoreId`() = runTest {
        val teamWithoutClubFirestore = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = 100L,
            clubFirestoreId = null,
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        every { getTeamUseCase() } returns flowOf(teamWithoutClubFirestore)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    @Test
    fun `should emit NoClub when user has team and team has no club`() = runTest {
        val teamWithoutClub = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = null,
            clubFirestoreId = null,
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        every { getTeamUseCase() } returns flowOf(teamWithoutClub)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    @Test
    fun `should emit TeamExists when user has team and team has clubFirestoreId`() = runTest {
        val clubMember = ClubMember(
            id = 1,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100,
            roles = listOf("Coach"),
            firestoreId = "clubmember_doc_123",
            clubFirestoreId = "club123",
        )
        val teamWithClub = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = null,
            clubFirestoreId = "club123",
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.TeamExists, viewModel.uiState.value)
    }

    @Test
    fun `should synchronize time on startup`() = runTest {
        every { getCurrentUserUseCase() } returns flowOf(null)

        createViewModel()
        advanceUntilIdle()

        coVerify { synchronizeTimeUseCase() }
    }

    @Test
    fun `should continue with authentication check even if time sync fails`() = runTest {
        coEvery { synchronizeTimeUseCase() } throws Exception("Test exception")
        every { getCurrentUserUseCase() } returns flowOf(null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)
        coVerify { synchronizeTimeUseCase() }
    }

    @Test
    fun `refresh should reset to Loading and re-run startup tasks`() = runTest {
        every { getCurrentUserUseCase() } returns flowOf(null)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)

        viewModel.refresh()

        assertEquals(SplashViewModel.UiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertEquals(SplashViewModel.UiState.NotAuthenticated, viewModel.uiState.value)
    }

    @Test
    fun `syncFcmToken is called with clubFirestoreId when TeamExists and permission is granted`() = runTest {
        every { isNotificationPermissionGranted() } returns true
        val teamWithClub = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = null,
            clubFirestoreId = "club123",
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        createViewModel()
        advanceUntilIdle()

        coVerify { syncFcmTokenUseCase("user123", "android", "club123") }
    }

    @Test
    fun `syncFcmToken is NOT called when TeamExists and permission is denied`() = runTest {
        every { isNotificationPermissionGranted() } returns false
        val teamWithClub = Team(
            id = 1,
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = null,
            clubFirestoreId = "club123",
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { syncFcmTokenUseCase(any(), any(), any()) }
    }

    @Test
    fun `syncFcmToken is called with clubFirestoreId when ClubPresident and permission is granted`() = runTest {
        every { isNotificationPermissionGranted() } returns true
        val clubMember = ClubMember(
            id = 1,
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = 100,
            roles = listOf("Presidente"),
            firestoreId = "clubmember_doc_123",
            clubFirestoreId = "club123",
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)

        createViewModel()
        advanceUntilIdle()

        coVerify { syncFcmTokenUseCase("user123", "android", "club123") }
    }

    @Test
    fun `syncFcmToken is NOT called when user has no club`() = runTest {
        every { isNotificationPermissionGranted() } returns true
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(null)

        createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { syncFcmTokenUseCase(any(), any(), any()) }
    }
}
