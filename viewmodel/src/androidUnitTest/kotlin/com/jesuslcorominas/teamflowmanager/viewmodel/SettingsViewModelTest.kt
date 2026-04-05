package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    private lateinit var getActiveViewRoleUseCase: GetActiveViewRoleUseCase
    private lateinit var setActiveViewRoleUseCase: SetActiveViewRoleUseCase
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
        getActiveViewRoleUseCase = mockk()
        setActiveViewRoleUseCase = mockk(relaxed = true)

        every { getCurrentUserUseCase() } returns flowOf(null)
        every { getTeamUseCase() } returns flowOf(null)
        every { getUserClubMembershipUseCase() } returns flowOf(null)
        every { getActiveViewRoleUseCase() } returns ActiveViewRole.President

        viewModel = SettingsViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            signOutUseCase = signOutUseCase,
            deleteFcmTokenUseCase = deleteFcmTokenUseCase,
            analyticsTracker = analyticsTracker,
            getTeam = getTeamUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            getActiveViewRole = getActiveViewRoleUseCase,
            setActiveViewRole = setActiveViewRoleUseCase,
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
            getActiveViewRole = getActiveViewRoleUseCase,
            setActiveViewRole = setActiveViewRoleUseCase,
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
                id = 1,
                userId = "user123",
                name = "Test User",
                email = "test@example.com",
                clubId = 100,
                roles = listOf("Coach"),
                firestoreId = "clubmember_doc_123",
                clubFirestoreId = "club123",
            ),
        )
        viewModel = SettingsViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            signOutUseCase = signOutUseCase,
            deleteFcmTokenUseCase = deleteFcmTokenUseCase,
            analyticsTracker = analyticsTracker,
            getTeam = getTeamUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            getActiveViewRole = getActiveViewRoleUseCase,
            setActiveViewRole = setActiveViewRoleUseCase,
        )
        advanceUntilIdle()

        assertFalse(viewModel.roleSelectorState.value.showRoleSelector)
    }

    @Test
    fun `roleSelectorState showRoleSelector is false when president has no team`() = runTest(testDispatcher) {
        every { getUserClubMembershipUseCase() } returns flowOf(
            ClubMember(
                id = 1,
                userId = "user123",
                name = "Test User",
                email = "test@example.com",
                clubId = 100,
                roles = listOf("Presidente"),
                firestoreId = "clubmember_doc_123",
                clubFirestoreId = "club123",
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
            getActiveViewRole = getActiveViewRoleUseCase,
            setActiveViewRole = setActiveViewRoleUseCase,
        )
        advanceUntilIdle()

        assertFalse(viewModel.roleSelectorState.value.showRoleSelector)
    }

    @Test
    fun `roleSelectorState showRoleSelector is true when president has a team`() = runTest(testDispatcher) {
        every { getUserClubMembershipUseCase() } returns flowOf(
            ClubMember(
                id = 1,
                userId = "user123",
                name = "Test User",
                email = "test@example.com",
                clubId = 100,
                roles = listOf("Presidente"),
                firestoreId = "clubmember_doc_123",
                clubFirestoreId = "club123",
            ),
        )
        every { getTeamUseCase() } returns flowOf(
            Team(
                id = 1,
                name = "Test Team",
                coachName = "Coach",
                delegateName = "Delegate",
                teamType = TeamType.FOOTBALL_5,
                clubId = 100L,
                clubFirestoreId = "club123",
            ),
        )
        viewModel = SettingsViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            signOutUseCase = signOutUseCase,
            deleteFcmTokenUseCase = deleteFcmTokenUseCase,
            analyticsTracker = analyticsTracker,
            getTeam = getTeamUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            getActiveViewRole = getActiveViewRoleUseCase,
            setActiveViewRole = setActiveViewRoleUseCase,
        )
        advanceUntilIdle()

        assertTrue(viewModel.roleSelectorState.value.showRoleSelector)
        assertEquals(ActiveViewRole.President, viewModel.roleSelectorState.value.activeRole)
    }

    @Test
    fun `onRoleSelected updates activeRole and fires roleChangedEvent`() = runTest(testDispatcher) {
        viewModel.onRoleSelected(ActiveViewRole.Coach)

        assertTrue(viewModel.roleSelectorState.value.roleChangedEvent)
        assertEquals(ActiveViewRole.Coach, viewModel.roleSelectorState.value.activeRole)
    }

    @Test
    fun `onRoleChangedEventConsumed resets roleChangedEvent`() = runTest(testDispatcher) {
        viewModel.onRoleSelected(ActiveViewRole.Coach)
        assertTrue(viewModel.roleSelectorState.value.roleChangedEvent)

        viewModel.onRoleChangedEventConsumed()

        assertFalse(viewModel.roleSelectorState.value.roleChangedEvent)
    }
}
