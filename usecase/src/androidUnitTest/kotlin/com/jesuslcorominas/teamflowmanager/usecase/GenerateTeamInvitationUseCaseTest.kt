package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GenerateTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GenerateTeamInvitationUseCaseTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var clubMemberRepository: ClubMemberRepository
    private lateinit var getCurrentUser: GetCurrentUserUseCase
    private lateinit var useCase: GenerateTeamInvitationUseCase

    private val user = User(id = "user1", email = "president@test.com", displayName = "President", photoUrl = null)
    private val team = Team(id = 1L, name = "Team A", coachName = "Coach", delegateName = "Del", teamType = TeamType.FOOTBALL_7, firestoreId = "team_fs_1", clubFirestoreId = "club_fs_1")
    private val presidentMember = ClubMember(id = 1L, userId = "user1", name = "President", email = "president@test.com", clubId = 10L, roles = listOf(ClubRole.PRESIDENT.roleName), clubFirestoreId = "club_fs_1")

    @Before
    fun setup() {
        teamRepository = mockk()
        clubMemberRepository = mockk()
        getCurrentUser = mockk()
        useCase = GenerateTeamInvitationUseCaseImpl(teamRepository, clubMemberRepository, getCurrentUser)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenBlankTeamFirestoreId_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        useCase.invoke("", "Team A")
    }

    @Test(expected = IllegalStateException::class)
    fun `givenUnauthenticatedUser_whenInvoke_thenThrowIllegalStateException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(null)
        useCase.invoke("team_fs_1", "Team A")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenTeamNotFound_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(user)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns null
        useCase.invoke("team_fs_1", "Team A")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenTeamNotInClub_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val teamWithoutClub = team.copy(clubFirestoreId = null)
        coEvery { getCurrentUser() } returns flowOf(user)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns teamWithoutClub
        useCase.invoke("team_fs_1", "Team A")
    }

    @Test(expected = IllegalStateException::class)
    fun `givenUserNotClubMember_whenInvoke_thenThrowIllegalStateException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(user)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(null)
        useCase.invoke("team_fs_1", "Team A")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenNonPresidentUser_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val staffMember = presidentMember.copy(roles = listOf(ClubRole.STAFF.roleName))
        coEvery { getCurrentUser() } returns flowOf(user)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(staffMember)
        useCase.invoke("team_fs_1", "Team A")
    }

    @Test
    fun `givenPresidentUserAndValidTeam_whenInvoke_thenReturnInvitationLink`() = runTest {
        val invitationLink = "https://invite.example.com/teamA"
        coEvery { getCurrentUser() } returns flowOf(user)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(presidentMember)
        coEvery { teamRepository.generateTeamInvitationLink("team_fs_1", "Team A") } returns invitationLink

        val result = useCase.invoke("team_fs_1", "Team A")

        assertEquals(invitationLink, result)
    }
}
