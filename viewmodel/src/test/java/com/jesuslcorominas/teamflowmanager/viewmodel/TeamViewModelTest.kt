package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import androidx.lifecycle.SavedStateHandle
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.HasScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetPlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateTeamUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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

@ExperimentalCoroutinesApi
class TeamViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getTeamUseCase: GetTeamUseCase
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var createTeamUseCase: CreateTeamUseCase
    private lateinit var updateTeamUseCase: UpdateTeamUseCase
    private lateinit var getCaptainPlayerUseCase: GetCaptainPlayerUseCase
    private lateinit var hasScheduledMatchesUseCase: HasScheduledMatchesUseCase
    private lateinit var setPlayerAsCaptainUseCase: SetPlayerAsCaptainUseCase
    private lateinit var removePlayerAsCaptainUseCase: RemovePlayerAsCaptainUseCase
    private lateinit var getUserClubMembershipUseCase: GetUserClubMembershipUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: TeamViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamUseCase = mockk()
        getPlayersUseCase = mockk()
        createTeamUseCase = mockk(relaxed = true)
        updateTeamUseCase = mockk(relaxed = true)
        getCaptainPlayerUseCase = mockk()
        hasScheduledMatchesUseCase = mockk()
        setPlayerAsCaptainUseCase = mockk(relaxed = true)
        removePlayerAsCaptainUseCase = mockk(relaxed = true)
        getUserClubMembershipUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)
        savedStateHandle = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getTeamUseCase.invoke() } returns flowOf(null)
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)

        // When
        viewModel = TeamViewModel(
            getTeam = getTeamUseCase,
            getPlayers = getPlayersUseCase,
            createTeam = createTeamUseCase,
            updateTeam = updateTeamUseCase,
            getCaptainPlayer = getCaptainPlayerUseCase,
            hasScheduledMatches = hasScheduledMatchesUseCase,
            setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
            removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            analyticsTracker = analyticsTracker,
            savedStateHandle = savedStateHandle
        )

        // Then
        assertEquals(TeamUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be NoTeam when no team exists`() =
        runTest(testDispatcher) {
            // Given
            every { getTeamUseCase.invoke() } returns flowOf(null)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)

            // When
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                getUserClubMembership = getUserClubMembershipUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle
            )
            advanceUntilIdle()

            // Then
            assert(viewModel.uiState.value is TeamUiState.NoTeam)
        }

    @Test
    fun `uiState should be Success when team exists`() =
        runTest(testDispatcher) {
            // Given
            val team = Team(1, "Test Team", "Coach Name", "Delegate Name", teamType = TeamType.FOOTBALL_5)
            every { getTeamUseCase.invoke() } returns flowOf(team)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)

            // When
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                getUserClubMembership = getUserClubMembershipUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle
            )
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(TeamUiState.Success(team, emptyList()), state)
        }

    @Test
    fun `createTeam should call createTeamUseCase with correct parameters`() =
        runTest(testDispatcher) {
            // Given
            val team = Team(0, "Test Team", "Coach Name", "Delegate Name", teamType = TeamType.FOOTBALL_5)
            every { getTeamUseCase.invoke() } returns flowOf(null)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)
            coEvery { createTeamUseCase.invoke(any()) } just runs
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                getUserClubMembership = getUserClubMembershipUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle
            )
            val onSuccess: () -> Unit = mockk(relaxed = true)

            // When
            viewModel.createTeam(team, onSuccess)
            advanceUntilIdle()

            // Then
            coVerify { createTeamUseCase.invoke(team) }
        }

    @Test
    fun `updateTeam should call updateTeamUseCase with correct team`() =
        runTest(testDispatcher) {
            // Given
            val team = Team(1, "Updated Team", "Updated Coach", "Updated Delegate", teamType = TeamType.FOOTBALL_5)
            every { getTeamUseCase.invoke() } returns flowOf(team)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)
            coEvery { updateTeamUseCase.invoke(any()) } just runs
            coEvery { getCaptainPlayerUseCase.invoke() } returns null
            coEvery { hasScheduledMatchesUseCase.invoke() } returns false
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                getUserClubMembership = getUserClubMembershipUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle
            )
            val onSuccess: () -> Unit = mockk(relaxed = true)

            // When
            viewModel.updateTeam(team, null, onSuccess)
            advanceUntilIdle()

            // Then
            coVerify { updateTeamUseCase.invoke(team) }
        }

    @Test
    fun `NoTeam state should include club info when user is President`() =
        runTest(testDispatcher) {
            // Given
            val clubMember = ClubMember(
                id = 1,
                userId = "user123",
                name = "John Doe",
                email = "john@example.com",
                clubId = 100,
                roles = listOf("Presidente"),
                firestoreId = "clubmember_doc_123",
                clubFirestoreId = "club_firestore_123"
            )
            every { getTeamUseCase.invoke() } returns flowOf(null)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(clubMember)

            // When
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                getUserClubMembership = getUserClubMembershipUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle
            )
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assert(state is TeamUiState.NoTeam)
            val noTeamState = state as TeamUiState.NoTeam
            assertEquals(100L, noTeamState.clubId)
            assertEquals("club_firestore_123", noTeamState.clubFirestoreId)
            assertEquals(true, noTeamState.isPresident)
            assertEquals(ClubRole.PRESIDENT, noTeamState.userRole)
        }

    @Test
    fun `NoTeam state should mark isPresident as false when user is not President`() =
        runTest(testDispatcher) {
            // Given
            val clubMember = ClubMember(
                id = 1,
                userId = "user123",
                name = "John Doe",
                email = "john@example.com",
                clubId = 100,
                roles = listOf("Coach"),
                firestoreId = "clubmember_doc_123",
                clubFirestoreId = "club_firestore_123"
            )
            every { getTeamUseCase.invoke() } returns flowOf(null)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(clubMember)

            // When
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                getUserClubMembership = getUserClubMembershipUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle
            )
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assert(state is TeamUiState.NoTeam)
            val noTeamState = state as TeamUiState.NoTeam
            assertEquals(100L, noTeamState.clubId)
            assertEquals("club_firestore_123", noTeamState.clubFirestoreId)
            assertEquals(false, noTeamState.isPresident)
            assertEquals(ClubRole.COACH, noTeamState.userRole)
        }

    private fun createViewModelWithTeam(team: Team? = null): TeamViewModel {
        every { getTeamUseCase.invoke() } returns flowOf(team)
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)
        return TeamViewModel(
            getTeam = getTeamUseCase,
            getPlayers = getPlayersUseCase,
            createTeam = createTeamUseCase,
            updateTeam = updateTeamUseCase,
            getCaptainPlayer = getCaptainPlayerUseCase,
            hasScheduledMatches = hasScheduledMatchesUseCase,
            setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
            removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            analyticsTracker = analyticsTracker,
            savedStateHandle = savedStateHandle,
        )
    }

    @Test
    fun `showTeamTypeChangeError should set showTeamTypeChangeError to true`() =
        runTest(testDispatcher) {
            viewModel = createViewModelWithTeam()
            advanceUntilIdle()

            viewModel.showTeamTypeChangeError()

            assert(viewModel.showTeamTypeChangeError.value)
        }

    @Test
    fun `dismissTeamTypeChangeError should set showTeamTypeChangeError to false`() =
        runTest(testDispatcher) {
            viewModel = createViewModelWithTeam()
            advanceUntilIdle()
            viewModel.showTeamTypeChangeError()

            viewModel.dismissTeamTypeChangeError()

            assert(!viewModel.showTeamTypeChangeError.value)
        }

    @Test
    fun `dismissSaveError should set showSaveError to false`() = runTest(testDispatcher) {
        viewModel = createViewModelWithTeam()
        advanceUntilIdle()
        coEvery { createTeamUseCase.invoke(any()) } throws Exception("Save failed")
        val team = Team(0, "Team", "Coach", "Delegate", teamType = TeamType.FOOTBALL_5)
        viewModel.createTeam(team) {}
        advanceUntilIdle()

        viewModel.dismissSaveError()

        assert(!viewModel.showSaveError.value)
    }

    @Test
    fun `createTeam should set showSaveError on exception`() = runTest(testDispatcher) {
        viewModel = createViewModelWithTeam()
        advanceUntilIdle()
        coEvery { createTeamUseCase.invoke(any()) } throws Exception("Error")
        val team = Team(0, "Team", "Coach", "Delegate", teamType = TeamType.FOOTBALL_5)

        viewModel.createTeam(team) {}
        advanceUntilIdle()

        assert(viewModel.showSaveError.value)
    }

    @Test
    fun `requestBack in non-edit mode should navigate back without dialog`() =
        runTest(testDispatcher) {
            every { savedStateHandle.get<String>("mode") } returns null
            viewModel = createViewModelWithTeam()
            advanceUntilIdle()
            var navigatedBack = false

            viewModel.requestBack { navigatedBack = true }

            assert(navigatedBack)
            assert(!viewModel.showExitDialog.value)
        }

    @Test
    fun `requestBack in edit mode should show exit dialog`() = runTest(testDispatcher) {
        every { savedStateHandle.get<String>("mode") } returns "edit"
        viewModel = createViewModelWithTeam()
        advanceUntilIdle()
        var navigatedBack = false

        viewModel.requestBack { navigatedBack = true }

        assert(!navigatedBack)
        assert(viewModel.showExitDialog.value)
    }

    @Test
    fun `discardChanges should hide dialog and navigate back`() = runTest(testDispatcher) {
        every { savedStateHandle.get<String>("mode") } returns "edit"
        viewModel = createViewModelWithTeam()
        advanceUntilIdle()
        viewModel.requestBack { }
        var navigatedBack = false

        viewModel.discardChanges { navigatedBack = true }

        assert(navigatedBack)
        assert(!viewModel.showExitDialog.value)
    }

    @Test
    fun `dismissExitDialog should set showExitDialog to false`() = runTest(testDispatcher) {
        every { savedStateHandle.get<String>("mode") } returns "edit"
        viewModel = createViewModelWithTeam()
        advanceUntilIdle()
        viewModel.requestBack { }

        viewModel.dismissExitDialog()

        assert(!viewModel.showExitDialog.value)
    }

    @Test
    fun `updateTeam should show error when team type changed and scheduled matches exist`() =
        runTest(testDispatcher) {
            val originalTeam = Team(1, "Team", "Coach", "Delegate", teamType = TeamType.FOOTBALL_5)
            val updatedTeam = Team(1, "Team", "Coach", "Delegate", teamType = TeamType.FOOTBALL_7)
            every { getTeamUseCase.invoke() } returns flowOf(originalTeam)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)
            coEvery { hasScheduledMatchesUseCase.invoke() } returns true
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                getUserClubMembership = getUserClubMembershipUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle,
            )
            advanceUntilIdle()

            viewModel.updateTeam(updatedTeam, null) {}
            advanceUntilIdle()

            assert(viewModel.showTeamTypeChangeError.value)
        }

    @Test
    fun `updateTeam should set captain when captainId provided and no current captain`() =
        runTest(testDispatcher) {
            val team = Team(1, "Team", "Coach", "Delegate", teamType = TeamType.FOOTBALL_5)
            every { getTeamUseCase.invoke() } returns flowOf(team)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)
            coEvery { hasScheduledMatchesUseCase.invoke() } returns false
            coEvery { getCaptainPlayerUseCase.invoke() } returns null
            coEvery { updateTeamUseCase.invoke(any()) } just runs
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                getUserClubMembership = getUserClubMembershipUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle,
            )
            advanceUntilIdle()

            viewModel.updateTeam(team, 5L) {}
            advanceUntilIdle()

            coVerify { setPlayerAsCaptainUseCase.invoke(5L) }
        }

    @Test
    fun `updateTeam should remove captain when captainId is null and captain exists`() =
        runTest(testDispatcher) {
            val team = Team(1, "Team", "Coach", "Delegate", teamType = TeamType.FOOTBALL_5)
            val existingCaptain = Player(
                id = 3L,
                firstName = "Cap",
                lastName = "Tain",
                number = 1,
                positions = emptyList(),
                teamId = 1L,
                isCaptain = true,
            )
            every { getTeamUseCase.invoke() } returns flowOf(team)
            every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)
            coEvery { hasScheduledMatchesUseCase.invoke() } returns false
            coEvery { getCaptainPlayerUseCase.invoke() } returns existingCaptain
            coEvery { updateTeamUseCase.invoke(any()) } just runs
            viewModel = TeamViewModel(
                getTeam = getTeamUseCase,
                getPlayers = getPlayersUseCase,
                createTeam = createTeamUseCase,
                updateTeam = updateTeamUseCase,
                getCaptainPlayer = getCaptainPlayerUseCase,
                hasScheduledMatches = hasScheduledMatchesUseCase,
                setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
                removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
                getUserClubMembership = getUserClubMembershipUseCase,
                analyticsTracker = analyticsTracker,
                savedStateHandle = savedStateHandle,
            )
            advanceUntilIdle()

            viewModel.updateTeam(team, null) {}
            advanceUntilIdle()

            coVerify { removePlayerAsCaptainUseCase.invoke(3L) }
        }

    @Test
    fun `updateTeam should set showSaveError on exception`() = runTest(testDispatcher) {
        val team = Team(1, "Team", "Coach", "Delegate", teamType = TeamType.FOOTBALL_5)
        every { getTeamUseCase.invoke() } returns flowOf(team)
        every { getPlayersUseCase.invoke() } returns flowOf(emptyList<Player>())
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)
        coEvery { hasScheduledMatchesUseCase.invoke() } returns false
        coEvery { getCaptainPlayerUseCase.invoke() } throws Exception("Error")
        viewModel = TeamViewModel(
            getTeam = getTeamUseCase,
            getPlayers = getPlayersUseCase,
            createTeam = createTeamUseCase,
            updateTeam = updateTeamUseCase,
            getCaptainPlayer = getCaptainPlayerUseCase,
            hasScheduledMatches = hasScheduledMatchesUseCase,
            setPlayerAsCaptainUseCase = setPlayerAsCaptainUseCase,
            removePlayerAsCaptainUseCase = removePlayerAsCaptainUseCase,
            getUserClubMembership = getUserClubMembershipUseCase,
            analyticsTracker = analyticsTracker,
            savedStateHandle = savedStateHandle,
        )
        advanceUntilIdle()

        viewModel.updateTeam(team, null) {}
        advanceUntilIdle()

        assert(viewModel.showSaveError.value)
    }
}
