package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.IsNotificationPermissionGrantedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RecoverAbandonedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SyncFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.AppCoroutineScope
import androidx.lifecycle.ViewModelStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    private lateinit var getActiveViewRoleUseCase: GetActiveViewRoleUseCase
    private lateinit var recoverAbandonedMatches: RecoverAbandonedMatchesUseCase
    private lateinit var appScope: AppCoroutineScope

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
        getActiveViewRoleUseCase = mockk()
        recoverAbandonedMatches = mockk(relaxed = true)
        // Un scope de aplicación de verdad: sobrevive a que el ViewModel se limpie, pero comparte
        // el scheduler del test para que advanceUntilIdle() lo gobierne.
        appScope = object : AppCoroutineScope, CoroutineScope by CoroutineScope(testDispatcher) {}
        coEvery { synchronizeTimeUseCase() } returns Unit
        every { isNotificationPermissionGranted() } returns false
        every { getActiveViewRoleUseCase() } returns ActiveViewRole.President
    }

    @After
    fun tearDown() {
        appScope.cancel()
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SplashViewModel(
        getTeam = getTeamUseCase,
        getCurrentUser = getCurrentUserUseCase,
        getUserClubMembership = getUserClubMembershipUseCase,
        synchronizeTimeUseCase = synchronizeTimeUseCase,
        syncFcmTokenUseCase = syncFcmTokenUseCase,
        isNotificationPermissionGranted = isNotificationPermissionGranted,
        getActiveViewRole = getActiveViewRoleUseCase,
        recoverAbandonedMatches = recoverAbandonedMatches,
        appScope = appScope,
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
    fun `should emit NoClub when expelled member logs in again`() = runTest {
        // Expelled members have no clubMember document and their team's coachId is cleared
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        every { getTeamUseCase() } returns flowOf(null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    @Test
    fun `should emit ClubPresident when user has no team but is a President`() = runTest {
        val clubMember = ClubMember(
            id = "1",
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = "club123",
            roles = listOf("Presidente"),
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.ClubPresident, viewModel.uiState.value)
    }

    @Test
    fun `should emit NoTeam when user is a club member but has no team assigned`() = runTest {
        val clubMember = ClubMember(
            id = "1",
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = "club123",
            roles = listOf("Coach"),
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.NoTeam, viewModel.uiState.value)
    }

    @Test
    fun `should emit ClubPresident when President preference is President even if they own a team`() = runTest {
        every { getActiveViewRoleUseCase() } returns ActiveViewRole.President
        val clubMember = ClubMember(
            id = "1",
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = "club123",
            roles = listOf("Presidente"),
        )
        val teamWithClub = Team(
            id = "1",
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = "club123",
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.ClubPresident, viewModel.uiState.value)
    }

    @Test
    fun `should emit TeamExists when President preference is Coach and has a team with club`() = runTest {
        every { getActiveViewRoleUseCase() } returns ActiveViewRole.Coach
        val clubMember = ClubMember(
            id = "1",
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = "club123",
            roles = listOf("Presidente"),
        )
        val teamWithClub = Team(
            id = "1",
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = "club123",
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getUserClubMembershipUseCase() } returns flowOf(clubMember)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.TeamExists, viewModel.uiState.value)
    }

    @Test
    fun `should emit NoClub when user has team but team has no clubRemoteId`() = runTest {
        val teamWithoutClubFirestore = Team(
            id = "1",
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = null,
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
            id = "1",
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = null,
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        every { getTeamUseCase() } returns flowOf(teamWithoutClub)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    @Test
    fun `should emit TeamExists when user has team and team has clubRemoteId`() = runTest {
        val clubMember = ClubMember(
            id = "1",
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = "club123",
            roles = listOf("Coach"),
        )
        val teamWithClub = Team(
            id = "1",
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = "club123",
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
    fun `syncFcmToken is called with clubRemoteId when TeamExists and permission is granted`() = runTest {
        every { isNotificationPermissionGranted() } returns true
        val teamWithClub = Team(
            id = "1",
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = "club123",
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
            id = "1",
            name = "Test Team",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            clubId = "club123",
        )
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        every { getTeamUseCase() } returns flowOf(teamWithClub)

        createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { syncFcmTokenUseCase(any(), any(), any()) }
    }

    @Test
    fun `syncFcmToken is called with clubRemoteId when ClubPresident and permission is granted`() = runTest {
        every { isNotificationPermissionGranted() } returns true
        val clubMember = ClubMember(
            id = "1",
            userId = "user123",
            name = "Test User",
            email = "test@example.com",
            clubId = "club123",
            roles = listOf("Presidente"),
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

    @Test
    fun `should sweep abandoned matches once the user turns out to be authenticated`() = runTest {
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) { recoverAbandonedMatches() }
        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    @Test
    fun `should not sweep abandoned matches when the user is not authenticated`() = runTest {
        every { getCurrentUserUseCase() } returns flowOf(null)

        createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { recoverAbandonedMatches() }
    }

    @Test
    fun `should sweep only once even if the startup is refreshed`() = runTest {
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(null)

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        coVerify(exactly = 1) { recoverAbandonedMatches() }
    }

    @Test
    fun `should keep the navigation state when the sweep fails`() = runTest {
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        coEvery { recoverAbandonedMatches() } throws RuntimeException("boom")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SplashViewModel.UiState.NoClub, viewModel.uiState.value)
    }

    /**
     * Regresión del P0 de la revisión de la PR #464. El barrido corría en `viewModelScope`, y la
     * misma emisión de estado que lo lanza es la que despierta al splash para navegar; todas las
     * rutas hacen `popUpTo(Splash) { inclusive = true }`, lo que destruye el NavBackStackEntry y
     * cancela ese scope. Medido entonces: el barrido moría en su primera suspensión y no llegaba
     * ni a leer los partidos.
     */
    @Test
    fun `should finish the sweep even after the splash is popped off the back stack`() = runTest {
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        var entered = false
        var completed = false
        coEvery { recoverAbandonedMatches() } coAnswers {
            entered = true
            delay(100) // el round trip a Firestore del primer getAllMatches()
            completed = true
            0
        }

        val store = ViewModelStore()
        val viewModel = createViewModel()
        store.put("splash", viewModel)

        // Lo que hace SplashScreen: despertar con el primer estado no-Loading y navegar.
        viewModel.uiState.first { it !is SplashViewModel.UiState.Loading }
        // Lo que hace la navegación: el entry del splash se destruye y su ViewModel se limpia.
        store.clear()
        advanceUntilIdle()

        assertTrue("el barrido debe arrancar", entered)
        assertTrue("el barrido debe completar pese al pop del splash", completed)
    }
}
