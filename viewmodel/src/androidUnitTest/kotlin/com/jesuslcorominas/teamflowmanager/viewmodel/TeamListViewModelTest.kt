package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.AssignCoachToTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ClearTeamCoachUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreatePendingCoachAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeletePendingCoachAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GenerateTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubMembersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchesByTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamsByClubUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SelfAssignAsCoachUseCase
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeamListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getTeamsByClubUseCase: GetTeamsByClubUseCase
    private lateinit var getUserClubMembershipUseCase: GetUserClubMembershipUseCase
    private lateinit var generateTeamInvitationUseCase: GenerateTeamInvitationUseCase
    private lateinit var selfAssignAsCoachUseCase: SelfAssignAsCoachUseCase
    private lateinit var assignCoachToTeamUseCase: AssignCoachToTeamUseCase
    private lateinit var clearTeamCoachUseCase: ClearTeamCoachUseCase
    private lateinit var getClubMembersUseCase: GetClubMembersUseCase
    private lateinit var getMatchesByTeamUseCase: GetMatchesByTeamUseCase
    private lateinit var createPendingCoachAssignmentUseCase: CreatePendingCoachAssignmentUseCase
    private lateinit var deletePendingCoachAssignmentUseCase: DeletePendingCoachAssignmentUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamsByClubUseCase = mockk()
        getUserClubMembershipUseCase = mockk()
        generateTeamInvitationUseCase = mockk()
        selfAssignAsCoachUseCase = mockk(relaxed = true)
        assignCoachToTeamUseCase = mockk(relaxed = true)
        clearTeamCoachUseCase = mockk(relaxed = true)
        getClubMembersUseCase = mockk()
        getMatchesByTeamUseCase = mockk()
        createPendingCoachAssignmentUseCase = mockk(relaxed = true)
        deletePendingCoachAssignmentUseCase = mockk(relaxed = true)
        every { getClubMembersUseCase.invoke(any()) } returns flowOf(emptyList())
        every { getMatchesByTeamUseCase.invoke(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = TeamListViewModel(
        getTeamsByClub = getTeamsByClubUseCase,
        getUserClubMembership = getUserClubMembershipUseCase,
        generateTeamInvitation = generateTeamInvitationUseCase,
        selfAssignAsCoach = selfAssignAsCoachUseCase,
        assignCoachToTeam = assignCoachToTeamUseCase,
        clearTeamCoachUseCase = clearTeamCoachUseCase,
        getClubMembers = getClubMembersUseCase,
        getMatchesByTeam = getMatchesByTeamUseCase,
        createPendingCoachAssignment = createPendingCoachAssignmentUseCase,
        deletePendingCoachAssignment = deletePendingCoachAssignmentUseCase,
    )

    private fun presidentMember() = ClubMember(
        id = 1L,
        userId = "user1",
        name = "John Doe",
        email = "john@test.com",
        clubId = 100L,
        roles = listOf("Presidente"),
        firestoreId = "member1",
        clubFirestoreId = "club_fs_1",
    )

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)

        // When
        val viewModel = createViewModel()

        // Then
        assertEquals(TeamListViewModel.UiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be NoClubMembership when user has no membership`() =
        runTest(testDispatcher) {
            // Given
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(TeamListViewModel.UiState.NoClubMembership, viewModel.uiState.value)
        }

    @Test
    fun `uiState should be Success with teams when user has club membership`() =
        runTest(testDispatcher) {
            // Given
            val member = presidentMember()
            val team = Team(
                id = 1L,
                name = "Team A",
                coachName = "Coach",
                delegateName = "Delegate",
                teamType = TeamType.FOOTBALL_5,
                firestoreId = "team_fs_1",
            )
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
            every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(team))

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(
                TeamListViewModel.UiState.Success(listOf(team), member.name),
                viewModel.uiState.value,
            )
        }

    @Test
    fun `currentUserRole should be President for President member`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(emptyList())

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("Presidente", viewModel.currentUserRole.value)
    }

    @Test
    fun `currentUserRole should be first role for non-President member`() =
        runTest(testDispatcher) {
            // Given
            val member = ClubMember(
                id = 1L,
                userId = "user1",
                name = "John",
                email = "john@test.com",
                clubId = 100L,
                roles = listOf("Coach"),
                firestoreId = "member1",
                clubFirestoreId = "club_fs_1",
            )
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
            every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(emptyList())

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals("Coach", viewModel.currentUserRole.value)
        }

    @Test
    fun `shareTeam should emit ShareEvent and clear sharingTeamId after completion`() =
        runTest(testDispatcher) {
            // Given
            val member = presidentMember()
            val team = Team(
                id = 1L,
                name = "Team A",
                coachName = "Coach",
                delegateName = "Delegate",
                teamType = TeamType.FOOTBALL_5,
                firestoreId = "team_fs_1",
            )
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
            every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(team))
            coEvery {
                generateTeamInvitationUseCase.invoke("team_fs_1", "Team A")
            } returns "https://invite.link"
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.shareTeam(team)
            advanceUntilIdle()

            // Then
            assertEquals(
                TeamListViewModel.ShareEvent("https://invite.link", "Team A"),
                viewModel.shareEvent.value,
            )
            // sharingTeamId is cleared after completion
            assertNull(viewModel.sharingTeamId.value)
        }

    @Test
    fun `onShareEventConsumed should clear the share event`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val team = Team(
            id = 1L,
            name = "Team A",
            coachName = "Coach",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            firestoreId = "team_fs_1",
        )
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(team))
        coEvery { generateTeamInvitationUseCase.invoke(any(), any()) } returns "https://invite.link"
        val viewModel = createViewModel()
        viewModel.shareTeam(team)
        advanceUntilIdle()

        // When
        viewModel.onShareEventConsumed()

        // Then
        assertNull(viewModel.shareEvent.value)
    }

    @Test
    fun `selfAssignAsCoachToTeam should call use case and clear assigningCoachToTeamId`() =
        runTest(testDispatcher) {
            // Given
            val member = presidentMember()
            val team = Team(
                id = 1L,
                name = "Team A",
                coachName = "",
                delegateName = "Delegate",
                teamType = TeamType.FOOTBALL_5,
                firestoreId = "team_fs_1",
            )
            every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
            every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(team))
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.selfAssignAsCoachToTeam(team)
            advanceUntilIdle()

            // Then
            coVerify { selfAssignAsCoachUseCase.invoke("team_fs_1") }
            // assigningCoachToTeamId is cleared after completion
            assertNull(viewModel.assigningCoachToTeamId.value)
        }

    @Test
    fun `uiState should be Error when exception is thrown`() = runTest(testDispatcher) {
        // Given
        every { getUserClubMembershipUseCase.invoke() } throws RuntimeException("Error")

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(TeamListViewModel.UiState.Error, viewModel.uiState.value)
    }

    // region Search

    @Test
    fun `initial searchQuery should be empty`() = runTest(testDispatcher) {
        // Given
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)

        // When
        val viewModel = createViewModel()

        // Then
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `onSearchQueryChanged filters teams by name case insensitively`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val teamA = teamWithName("Alpha", firestoreId = "t1")
        val teamB = teamWithName("Beta", firestoreId = "t2")
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(teamA, teamB))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onSearchQueryChanged("alph")
        advanceUntilIdle()

        // Then
        assertEquals(
            TeamListViewModel.UiState.Success(listOf(teamA), member.name),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `onSearchQueryChanged with blank query shows all teams`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val teamA = teamWithName("Alpha", firestoreId = "t1")
        val teamB = teamWithName("Beta", firestoreId = "t2")
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(teamA, teamB))
        val viewModel = createViewModel()
        viewModel.onSearchQueryChanged("alph")
        advanceUntilIdle()

        // When
        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()

        // Then
        assertEquals(
            TeamListViewModel.UiState.Success(listOf(teamA, teamB), member.name),
            viewModel.uiState.value,
        )
    }

    // endregion

    // region CoachFilter

    @Test
    fun `initial coachFilter should be ALL`() = runTest(testDispatcher) {
        // Given
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)

        // When
        val viewModel = createViewModel()

        // Then
        assertEquals(TeamListViewModel.CoachFilter.ALL, viewModel.coachFilter.value)
    }

    @Test
    fun `onCoachFilterChanged ALL shows all teams regardless of coach`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val withCoach = teamWithName("With Coach", firestoreId = "t1", coachId = "c1")
        val withoutCoach = teamWithName("No Coach", firestoreId = "t2", coachId = null)
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(withCoach, withoutCoach))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onCoachFilterChanged(TeamListViewModel.CoachFilter.ALL)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as TeamListViewModel.UiState.Success
        assertEquals(listOf(withCoach, withoutCoach), state.teams)
    }

    @Test
    fun `onCoachFilterChanged WITH_COACH shows only teams that have a coach`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val withCoach = teamWithName("With Coach", firestoreId = "t1", coachId = "c1")
        val withoutCoach = teamWithName("No Coach", firestoreId = "t2", coachId = null)
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(withCoach, withoutCoach))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onCoachFilterChanged(TeamListViewModel.CoachFilter.WITH_COACH)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as TeamListViewModel.UiState.Success
        assertEquals(listOf(withCoach), state.teams)
    }

    @Test
    fun `onCoachFilterChanged WITHOUT_COACH shows only teams without a coach`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val withCoach = teamWithName("With Coach", firestoreId = "t1", coachId = "c1")
        val withoutCoach = teamWithName("No Coach", firestoreId = "t2", coachId = null)
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(withCoach, withoutCoach))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onCoachFilterChanged(TeamListViewModel.CoachFilter.WITHOUT_COACH)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as TeamListViewModel.UiState.Success
        assertEquals(listOf(withoutCoach), state.teams)
    }

    @Test
    fun `search and coach filter are applied together`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val alphaWithCoach = teamWithName("Alpha", firestoreId = "t1", coachId = "c1")
        val alphaNoCoach = teamWithName("Alpha B", firestoreId = "t2", coachId = null)
        val betaNoCoach = teamWithName("Beta", firestoreId = "t3", coachId = null)
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(alphaWithCoach, alphaNoCoach, betaNoCoach))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When: search "alpha" + filter WITHOUT_COACH
        viewModel.onSearchQueryChanged("alpha")
        viewModel.onCoachFilterChanged(TeamListViewModel.CoachFilter.WITHOUT_COACH)
        advanceUntilIdle()

        // Then: only alphaNoCoach matches both conditions
        val state = viewModel.uiState.value as TeamListViewModel.UiState.Success
        assertEquals(listOf(alphaNoCoach), state.teams)
    }

    // endregion

    // region Assign coach dialog

    @Test
    fun `requestAssignCoach sets assignCoachDialogTeam and clears error`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val team = teamWithName("Team A", firestoreId = "t1")
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(team))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.requestAssignCoach(team)

        // Then
        assertEquals(team, viewModel.assignCoachDialogTeam.value)
        assertNull(viewModel.assignCoachError.value)
    }

    @Test
    fun `dismissAssignCoachDialog clears dialog team and error`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val team = teamWithName("Team A", firestoreId = "t1")
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(team))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.requestAssignCoach(team)

        // When
        viewModel.dismissAssignCoachDialog()

        // Then
        assertNull(viewModel.assignCoachDialogTeam.value)
        assertNull(viewModel.assignCoachError.value)
    }

    @Test
    fun `assignCoachByMember calls use case and closes dialog`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val team = teamWithName("Team A", firestoreId = "t1")
        val coachMember = ClubMember(
            id = 2L, userId = "coach1", name = "Coach", email = "coach@test.com",
            clubId = 100L, roles = listOf("Coach"), firestoreId = "cm1", clubFirestoreId = "club_fs_1",
        )
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(team))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.requestAssignCoach(team)

        // When
        viewModel.assignCoachByMember(coachMember)
        advanceUntilIdle()

        // Then
        coVerify { assignCoachToTeamUseCase.invoke("t1", "coach1") }
        assertNull(viewModel.assignCoachDialogTeam.value)
        assertNull(viewModel.assignCoachError.value)
        assertNull(viewModel.assigningCoachToTeamId.value)
    }

    @Test
    fun `assignCoachByEmail assigns directly when email matches an existing member`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val team = teamWithName("Team A", firestoreId = "t1")
        val coachMember = ClubMember(
            id = 2L, userId = "coach1", name = "Coach", email = "coach@test.com",
            clubId = 100L, roles = listOf("Coach"), firestoreId = "cm1", clubFirestoreId = "club_fs_1",
        )
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(team))
        every { getClubMembersUseCase.invoke(any()) } returns flowOf(listOf(coachMember))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.requestAssignCoach(team)

        // When
        viewModel.assignCoachByEmail("coach@test.com")
        advanceUntilIdle()

        // Then — uses the member's userId, not the email string
        coVerify { assignCoachToTeamUseCase.invoke("t1", "coach1") }
        assertNull(viewModel.assignCoachDialogTeam.value)
    }

    @Test
    fun `assignCoachByEmail creates pending assignment for external email not in club`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val team = teamWithName("Team A", firestoreId = "t1")
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(team))
        // getClubMembersUseCase returns empty list (no members match the email)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.requestAssignCoach(team)

        // When
        viewModel.assignCoachByEmail("external@example.com")
        advanceUntilIdle()

        // Then — creates pending assignment, dismisses dialog, no error
        coVerify { createPendingCoachAssignmentUseCase.invoke("t1", "external@example.com") }
        coVerify(exactly = 0) { assignCoachToTeamUseCase.invoke(any(), any()) }
        assertNull(viewModel.assignCoachDialogTeam.value)
        assertNull(viewModel.assignCoachError.value)
    }

    @Test
    fun `deletePendingAssignment calls use case with team id`() = runTest(testDispatcher) {
        // Given
        val member = presidentMember()
        val team = teamWithName("Team A", firestoreId = "t1")
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(member)
        every { getTeamsByClubUseCase.invoke("club_fs_1") } returns flowOf(listOf(team))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.deletePendingAssignment(team)
        advanceUntilIdle()

        // Then
        coVerify { deletePendingCoachAssignmentUseCase.invoke("t1") }
    }

    @Test
    fun `clearAssignCoachError clears the error`() = runTest(testDispatcher) {
        // Given
        every { getUserClubMembershipUseCase.invoke() } returns flowOf(null)
        val viewModel = createViewModel()

        // When
        viewModel.clearAssignCoachError()

        // Then
        assertNull(viewModel.assignCoachError.value)
    }

    // endregion

    // region helpers

    private fun teamWithName(
        name: String,
        firestoreId: String,
        coachId: String? = null,
    ) = Team(
        id = firestoreId.hashCode().toLong(),
        name = name,
        coachName = if (coachId != null) "Coach" else "",
        delegateName = "",
        teamType = TeamType.FOOTBALL_5,
        firestoreId = firestoreId,
        coachId = coachId,
    )

    // endregion
}
