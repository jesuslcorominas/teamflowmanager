package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.GenerateTeamInvitationUseCase
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamsByClubUseCase = mockk()
        getUserClubMembershipUseCase = mockk()
        generateTeamInvitationUseCase = mockk()
        selfAssignAsCoachUseCase = mockk(relaxed = true)
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
}
