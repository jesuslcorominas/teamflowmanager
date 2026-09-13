package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObserveActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetNotificationPreferencesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateGlobalNotificationPreferenceUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var deleteFcmTokenUseCase: DeleteFcmTokenUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var getTeamUseCase: GetTeamUseCase
    private lateinit var getUserClubMembershipUseCase: GetUserClubMembershipUseCase
    private lateinit var observeActiveViewRoleUseCase: ObserveActiveViewRoleUseCase
    private lateinit var setActiveViewRoleUseCase: SetActiveViewRoleUseCase
    private lateinit var getNotificationPreferencesUseCase: GetNotificationPreferencesUseCase
    private lateinit var updateGlobalNotificationPreferenceUseCase: UpdateGlobalNotificationPreferenceUseCase
    private lateinit var crashReporter: CrashReporter
    private lateinit var viewModel: SettingsViewModel

    private val testUser = User(
        id = "user123",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCurrentUserUseCase = mockk()
        signOutUseCase = mockk()
        deleteFcmTokenUseCase = mockk(relaxed = true)
        analyticsTracker = mockk(relaxed = true)
        getTeamUseCase = mockk()
        getUserClubMembershipUseCase = mockk()
        observeActiveViewRoleUseCase = mockk()
        setActiveViewRoleUseCase = mockk(relaxed = true)
        getNotificationPreferencesUseCase = mockk(relaxed = true)
        updateGlobalNotificationPreferenceUseCase = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)

        every { getCurrentUserUseCase() } returns flowOf(null)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        every { observeActiveViewRoleUseCase() } returns flowOf(ActiveViewRole.President)

        viewModel = SettingsViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            signOutUseCase = signOutUseCase,
            deleteFcmTokenUseCase = deleteFcmTokenUseCase,
            analyticsTracker = analyticsTracker,
            getTeam = getTeamUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            observeActiveViewRole = observeActiveViewRoleUseCase,
            setActiveViewRole = setActiveViewRoleUseCase,
            getNotificationPreferences = getNotificationPreferencesUseCase,
            updateGlobalNotificationPreference = updateGlobalNotificationPreferenceUseCase,
            crashReporter = crashReporter,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial signOutComplete should be false`() {
        assertFalse(viewModel.signOutComplete.value)
    }

    @Test
    fun `signOut should call signOutUseCase and set signOutComplete`() = runTest(testDispatcher) {
        coEvery { signOutUseCase() } returns Unit

        viewModel.signOut()
        advanceUntilIdle()

        coVerify(exactly = 1) { signOutUseCase() }
        verify { analyticsTracker.logEvent("logout", any()) }
        verify { analyticsTracker.setUserId(null) }
        assertTrue(viewModel.signOutComplete.value)
    }

    @Test
    fun `clearSignOutComplete should reset signOutComplete to false`() = runTest(testDispatcher) {
        coEvery { signOutUseCase() } returns Unit
        viewModel.signOut()
        advanceUntilIdle()
        assertTrue(viewModel.signOutComplete.value)

        viewModel.clearSignOutComplete()

        assertFalse(viewModel.signOutComplete.value)
    }

    @Test
    fun `signOut should call deleteFcmTokenUseCase with userId when user is logged in`() = runTest(testDispatcher) {
        every { getCurrentUserUseCase() } returns flowOf(testUser)
        viewModel = SettingsViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            signOutUseCase = signOutUseCase,
            deleteFcmTokenUseCase = deleteFcmTokenUseCase,
            analyticsTracker = analyticsTracker,
            getTeam = getTeamUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            observeActiveViewRole = observeActiveViewRoleUseCase,
            setActiveViewRole = setActiveViewRoleUseCase,
            getNotificationPreferences = getNotificationPreferencesUseCase,
            updateGlobalNotificationPreference = updateGlobalNotificationPreferenceUseCase,
            crashReporter = crashReporter,
        )
        advanceUntilIdle()
        coEvery { signOutUseCase() } returns Unit

        viewModel.signOut()
        advanceUntilIdle()

        coVerify { deleteFcmTokenUseCase("user123") }
        coVerify { signOutUseCase() }
    }

    @Test
    fun `signOut should NOT call deleteFcmTokenUseCase when no user is logged in`() = runTest(testDispatcher) {
        coEvery { signOutUseCase() } returns Unit

        viewModel.signOut()
        advanceUntilIdle()

        coVerify(exactly = 0) { deleteFcmTokenUseCase(any()) }
        coVerify { signOutUseCase() }
    }

    @Test
    fun `roleSelectorState showRoleSelector is false when user is not a president`() = runTest(testDispatcher) {
        every { getUserClubMembershipUseCase() } returns flowOf(
            ClubMember(
                id = "1",
                userId = "user123",
                name = "Test User",
                email = "test@example.com",
                clubId = "club123",
                roles = listOf("Coach"),
            ),
        )
        viewModel = SettingsViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            signOutUseCase = signOutUseCase,
            deleteFcmTokenUseCase = deleteFcmTokenUseCase,
            analyticsTracker = analyticsTracker,
            getTeam = getTeamUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            observeActiveViewRole = observeActiveViewRoleUseCase,
            setActiveViewRole = setActiveViewRoleUseCase,
            getNotificationPreferences = getNotificationPreferencesUseCase,
            updateGlobalNotificationPreference = updateGlobalNotificationPreferenceUseCase,
            crashReporter = crashReporter,
        )
        advanceUntilIdle()

        assertFalse(viewModel.roleSelectorState.value.showRoleSelector)
    }

    @Test
    fun `roleSelectorState showRoleSelector is true but disabled when president has no team`() = runTest(testDispatcher) {
        every { getUserClubMembershipUseCase() } returns flowOf(
            ClubMember(
                id = "1",
                userId = "user123",
                name = "Test User",
                email = "test@example.com",
                clubId = "club123",
                roles = listOf("Presidente"),
            ),
        )
        every { getTeamUseCase() } returns flowOf(null)
        viewModel = SettingsViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            signOutUseCase = signOutUseCase,
            deleteFcmTokenUseCase = deleteFcmTokenUseCase,
            analyticsTracker = analyticsTracker,
            getTeam = getTeamUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            observeActiveViewRole = observeActiveViewRoleUseCase,
            setActiveViewRole = setActiveViewRoleUseCase,
            getNotificationPreferences = getNotificationPreferencesUseCase,
            updateGlobalNotificationPreference = updateGlobalNotificationPreferenceUseCase,
            crashReporter = crashReporter,
        )
        advanceUntilIdle()

        assertTrue(viewModel.roleSelectorState.value.showRoleSelector)
        assertFalse(viewModel.roleSelectorState.value.isRoleSelectorEnabled)
    }

    @Test
    fun `roleSelectorState showRoleSelector is true when president has a team`() = runTest(testDispatcher) {
        every { getUserClubMembershipUseCase() } returns flowOf(
            ClubMember(
                id = "1",
                userId = "user123",
                name = "Test User",
                email = "test@example.com",
                clubId = "club123",
                roles = listOf("Presidente"),
            ),
        )
        every { getTeamUseCase() } returns flowOf(
            Team(
                id = "1",
                name = "Test Team",
                coachName = "Coach",
                delegateName = "Delegate",
                teamType = TeamType.FOOTBALL_5,
                clubId = "club123",
            ),
        )
        viewModel = SettingsViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            signOutUseCase = signOutUseCase,
            deleteFcmTokenUseCase = deleteFcmTokenUseCase,
            analyticsTracker = analyticsTracker,
            getTeam = getTeamUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            observeActiveViewRole = observeActiveViewRoleUseCase,
            setActiveViewRole = setActiveViewRoleUseCase,
            getNotificationPreferences = getNotificationPreferencesUseCase,
            updateGlobalNotificationPreference = updateGlobalNotificationPreferenceUseCase,
            crashReporter = crashReporter,
        )
        advanceUntilIdle()

        assertTrue(viewModel.roleSelectorState.value.showRoleSelector)
        assertTrue(viewModel.roleSelectorState.value.isRoleSelectorEnabled)
        assertEquals(ActiveViewRole.President, viewModel.roleSelectorState.value.activeRole)
    }

    @Test
    fun `onRoleSelected does not apply the role until the selection is committed`() =
        runTest(testDispatcher) {
            val storedRole = MutableStateFlow<ActiveViewRole>(ActiveViewRole.President)
            buildPresidentViewModel(storedRole)
            advanceUntilIdle()

            // When — the switch is toggled several times, as a user comparing options might
            viewModel.onRoleSelected(ActiveViewRole.Coach)
            viewModel.onRoleSelected(ActiveViewRole.President)
            viewModel.onRoleSelected(ActiveViewRole.Coach)
            advanceUntilIdle()

            // Then — the switch follows, but nothing is applied: the shell must not move yet
            assertEquals(ActiveViewRole.Coach, viewModel.roleSelectorState.value.selectedRole)
            assertEquals(ActiveViewRole.President, viewModel.roleSelectorState.value.activeRole)
            verify(exactly = 0) { setActiveViewRoleUseCase(any()) }
        }

    @Test
    fun `commitRoleSelection applies only the final choice`() =
        runTest(testDispatcher) {
            val storedRole = MutableStateFlow<ActiveViewRole>(ActiveViewRole.President)
            buildPresidentViewModel(storedRole)
            advanceUntilIdle()

            viewModel.onRoleSelected(ActiveViewRole.Coach)
            viewModel.onRoleSelected(ActiveViewRole.President)
            viewModel.onRoleSelected(ActiveViewRole.Coach)
            viewModel.commitRoleSelection()
            advanceUntilIdle()

            verify(exactly = 1) { setActiveViewRoleUseCase(ActiveViewRole.Coach) }
            assertEquals(ActiveViewRole.Coach, viewModel.roleSelectorState.value.activeRole)
        }

    @Test
    fun `commitRoleSelection does nothing when the switch ended where it started`() =
        runTest(testDispatcher) {
            val storedRole = MutableStateFlow<ActiveViewRole>(ActiveViewRole.President)
            buildPresidentViewModel(storedRole)
            advanceUntilIdle()

            // Toggled and toggled back: leaving the screen must not count as a change
            viewModel.onRoleSelected(ActiveViewRole.Coach)
            viewModel.onRoleSelected(ActiveViewRole.President)
            viewModel.commitRoleSelection()
            advanceUntilIdle()

            verify(exactly = 0) { setActiveViewRoleUseCase(any()) }
        }

    @Test
    fun `givenPreferenceUpdateFails_whenUpdateGlobalMatchEvents_thenReportsAndSurfacesInsteadOfCrashing`() =
        runTest {
            // Given — the data source rethrows, as it does on any Firestore failure
            val failure = RuntimeException("permission denied")
            coEvery {
                updateGlobalNotificationPreferenceUseCase(any(), NotificationEventType.MATCH_EVENTS, any())
            } throws failure

            // When — this used to reach the default handler through viewModelScope and kill the app
            viewModel.updateGlobalMatchEvents(false)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.notificationUpdateFailed.value)
            verify { crashReporter.recordException(failure) }

            viewModel.onNotificationUpdateErrorShown()
            assertFalse(viewModel.notificationUpdateFailed.value)
        }

    @Test
    fun `givenPreferenceUpdateSucceeds_whenUpdateGlobalGoals_thenNoErrorIsSurfaced`() =
        runTest {
            viewModel.updateGlobalGoals(true)
            advanceUntilIdle()

            assertFalse(viewModel.notificationUpdateFailed.value)
            coVerify { updateGlobalNotificationPreferenceUseCase(any(), NotificationEventType.GOALS, true) }
        }

    /** A president with a team, so the role selector is shown and enabled. */
    private fun buildPresidentViewModel(storedRole: MutableStateFlow<ActiveViewRole>) {
        every { getUserClubMembershipUseCase() } returns flowOf(
            ClubMember(
                id = "1",
                userId = "user123",
                name = "Test User",
                email = "test@example.com",
                clubId = "club123",
                roles = listOf("Presidente"),
            ),
        )
        every { getTeamUseCase() } returns flowOf(
            Team(
                id = "1",
                name = "Test Team",
                coachName = "Coach",
                delegateName = "Delegate",
                teamType = TeamType.FOOTBALL_5,
                clubId = "club123",
            ),
        )
        every { observeActiveViewRoleUseCase() } returns storedRole
        every { setActiveViewRoleUseCase(any()) } answers { storedRole.value = firstArg() }

        viewModel = SettingsViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            signOutUseCase = signOutUseCase,
            deleteFcmTokenUseCase = deleteFcmTokenUseCase,
            analyticsTracker = analyticsTracker,
            getTeam = getTeamUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            observeActiveViewRole = observeActiveViewRoleUseCase,
            setActiveViewRole = setActiveViewRoleUseCase,
            getNotificationPreferences = getNotificationPreferencesUseCase,
            updateGlobalNotificationPreference = updateGlobalNotificationPreferenceUseCase,
            crashReporter = crashReporter,
        )
    }
}
