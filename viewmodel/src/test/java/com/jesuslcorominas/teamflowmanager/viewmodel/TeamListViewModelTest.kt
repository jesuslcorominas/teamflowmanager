package com.jesuslcorominas.teamflowmanager.viewmodel

import android.util.Log
import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.GenerateTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamsByClubUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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

    private lateinit var getTeamsByClub: GetTeamsByClubUseCase
    private lateinit var getUserClubMembership: GetUserClubMembershipUseCase
    private lateinit var generateTeamInvitation: GenerateTeamInvitationUseCase

    private lateinit var viewModel: TeamListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        getTeamsByClub = mockk()
        getUserClubMembership = mockk()
        generateTeamInvitation = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = TeamListViewModel(getTeamsByClub, getUserClubMembership, generateTeamInvitation)
    }

    @Test
    fun `initial state should be Loading`() {
        // Given
        every { getUserClubMembership() } returns flowOf(null)

        // When
        createViewModel()

        // Then
        assertEquals(TeamListViewModel.UiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState should be Success when teams are loaded`() = runTest {
        // Given
        val clubMember = ClubMember(1L, "user1", "Club Name", "email", 100L, "Presidente", "member1", "club1")
        val teams = listOf(
            Team(1L, "Team 1", "Coach", "Delegate", null, TeamType.FOOTBALL_5, "coach1", 100L, "club1", "team1")
        )
        every { getUserClubMembership() } returns flowOf(clubMember)
        every { getTeamsByClub("club1") } returns flowOf(teams)

        // When
        createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(TeamListViewModel.UiState.Success(teams, "Club Name"), state)
    }

    @Test
    fun `uiState should be NoClubMembership when user has no club`() = runTest {
        // Given
        every { getUserClubMembership() } returns flowOf(null)

        // When
        createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(TeamListViewModel.UiState.NoClubMembership, viewModel.uiState.value)
    }

    @Test
    fun `shareTeam should update shareEvent on success`() = runTest {
        // Given
        val clubMember = ClubMember(1L, "user1", "Club Name", "email", 100L, "Presidente", "member1", "club1")
        val teams = listOf(
            Team(1L, "Team 1", "Coach", "Delegate", null, TeamType.FOOTBALL_5, "coach1", 100L, "club1", "team1")
        )
        every { getUserClubMembership() } returns flowOf(clubMember)
        every { getTeamsByClub("club1") } returns flowOf(teams)
        coEvery { generateTeamInvitation("team1", "Team 1") } returns "link"

        createViewModel()
        advanceUntilIdle()

        // When
        viewModel.shareTeam(teams[0])
        advanceUntilIdle()

        // Then
        assertEquals(TeamListViewModel.ShareEvent("link", "Team 1"), viewModel.shareEvent.value)
    }

    @Test
    fun `onShareEventConsumed should clear shareEvent`() = runTest {
        // Given
        val clubMember = ClubMember(1L, "user1", "Club Name", "email", 100L, "Presidente", "member1", "club1")
        val teams = listOf(
            Team(1L, "Team 1", "Coach", "Delegate", null, TeamType.FOOTBALL_5, "coach1", 100L, "club1", "team1")
        )
        every { getUserClubMembership() } returns flowOf(clubMember)
        every { getTeamsByClub("club1") } returns flowOf(teams)
        coEvery { generateTeamInvitation("team1", "Team 1") } returns "link"

        createViewModel()
        advanceUntilIdle()
        viewModel.shareTeam(teams[0])
        advanceUntilIdle()
        assertEquals(TeamListViewModel.ShareEvent("link", "Team 1"), viewModel.shareEvent.value)

        // When
        viewModel.onShareEventConsumed()

        // Then
        assertNull(viewModel.shareEvent.value)
    }
}
