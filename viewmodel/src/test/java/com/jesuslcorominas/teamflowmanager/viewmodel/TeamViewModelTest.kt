package com.jesuslcorominas.teamflowmanager.viewmodel
import com.jesuslcorominas.teamflowmanager.domain.model.*

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import androidx.lifecycle.SavedStateHandle
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
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
            val team = Team(1L, "Test Team", "Coach Name", "Delegate Name", null, TeamType.FOOTBALL_5, "coach123")
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
            val team = Team(0L, "Test Team", "Coach Name", "Delegate Name", null, TeamType.FOOTBALL_5, "coach123")
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
            val team = Team(1L, "Updated Team", "Updated Coach", "Updated Delegate", null, TeamType.FOOTBALL_5, "coach123")
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
                id = 1L,
                userId = "user123",
                name = "John Doe",
                email = "john@example.com",
                clubId = 100L,
                role = "Presidente",
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
                id = 1L,
                userId = "user123",
                name = "John Doe",
                email = "john@example.com",
                clubId = 100L,
                role = "Coach",
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
}
