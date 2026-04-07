package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.AssignCoachToTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyCoachAssignedOnTeamAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AssignCoachToTeamUseCaseTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var clubMemberRepository: ClubMemberRepository
    private lateinit var getCurrentUser: GetCurrentUserUseCase
    private lateinit var notifyCoachAssigned: NotifyCoachAssignedOnTeamAssignmentUseCase
    private lateinit var useCase: AssignCoachToTeamUseCase

    private val president = User(id = "user1", email = "pres@test.com", displayName = "President", photoUrl = null)
    private val team = Team(id = 1L, name = "Team A", coachName = "", delegateName = "Del", teamType = TeamType.FOOTBALL_7, firestoreId = "team_fs_1", clubFirestoreId = "club_fs_1")
    private val presidentMember = ClubMember(id = 1L, userId = "user1", name = "President", email = "pres@test.com", clubId = 10L, roles = listOf(ClubRole.PRESIDENT.roleName), clubFirestoreId = "club_fs_1")
    private val coachMember = ClubMember(id = 2L, userId = "coach1", name = "Coach", email = "coach@test.com", clubId = 10L, roles = listOf(ClubRole.STAFF.roleName), clubFirestoreId = "club_fs_1")

    @Before
    fun setup() {
        teamRepository = mockk(relaxed = true)
        clubMemberRepository = mockk(relaxed = true)
        getCurrentUser = mockk()
        notifyCoachAssigned = mockk(relaxed = true)
        useCase = AssignCoachToTeamUseCaseImpl(teamRepository, clubMemberRepository, getCurrentUser, notifyCoachAssigned)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenBlankTeamFirestoreId_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        useCase.invoke("", "coach1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenBlankCoachUserId_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        useCase.invoke("team_fs_1", "")
    }

    @Test(expected = IllegalStateException::class)
    fun `givenUnauthenticatedUser_whenInvoke_thenThrowIllegalStateException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(null)
        useCase.invoke("team_fs_1", "coach1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenTeamNotFound_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns null
        useCase.invoke("team_fs_1", "coach1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenNonPresidentUser_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val staffMember = presidentMember.copy(roles = listOf(ClubRole.STAFF.roleName))
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(staffMember)
        useCase.invoke("team_fs_1", "coach1")
    }

    @Test
    fun `givenPresidentAndValidCoach_whenInvoke_thenAssignCoachAndAddRole`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(presidentMember)
        coEvery { clubMemberRepository.getClubMemberByUserIdAndClub("coach1", "club_fs_1") } returns coachMember

        val result = useCase.invoke("team_fs_1", "coach1")

        assertEquals("coach1", result.coachId)
        coVerify { teamRepository.updateTeamCoachId("team_fs_1", "coach1") }
        coVerify { clubMemberRepository.addClubMemberRole("coach1", "club_fs_1", ClubRole.COACH.roleName) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenTeamWithoutClub_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val teamWithoutClub = team.copy(clubFirestoreId = null)
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns teamWithoutClub
        useCase.invoke("team_fs_1", "coach1")
    }

    @Test(expected = IllegalStateException::class)
    fun `givenCurrentUserNotClubMember_whenInvoke_thenThrowIllegalStateException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(null)
        useCase.invoke("team_fs_1", "coach1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenPresidentInDifferentClubThanTeam_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val memberInDifferentClub = presidentMember.copy(clubFirestoreId = "different_club_fs")
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(memberInDifferentClub)
        useCase.invoke("team_fs_1", "coach1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenCoachNotClubMember_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(presidentMember)
        coEvery { clubMemberRepository.getClubMemberByUserIdAndClub("coach1", "club_fs_1") } returns null
        useCase.invoke("team_fs_1", "coach1")
    }

    @Test(expected = IllegalStateException::class)
    fun `givenRepositoryThrowsOnTeamUpdate_whenInvoke_thenThrowIllegalStateException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(presidentMember)
        coEvery { clubMemberRepository.getClubMemberByUserIdAndClub("coach1", "club_fs_1") } returns coachMember
        coEvery { teamRepository.updateTeamCoachId(any(), any()) } throws RuntimeException("Network error")
        useCase.invoke("team_fs_1", "coach1")
    }

    @Test
    fun `givenPresidentAndValidCoach_whenInvoke_thenNotifyCoachIsCalled`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(presidentMember)
        coEvery { clubMemberRepository.getClubMemberByUserIdAndClub("coach1", "club_fs_1") } returns coachMember

        useCase.invoke("team_fs_1", "coach1")

        coVerify { notifyCoachAssigned(coachUserId = "coach1", assignedByUserId = "user1", teamName = "Team A") }
    }

    @Test
    fun `givenNotifyCoachThrows_whenInvoke_thenAssignmentStillSucceeds`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(presidentMember)
        coEvery { clubMemberRepository.getClubMemberByUserIdAndClub("coach1", "club_fs_1") } returns coachMember
        coEvery { notifyCoachAssigned(any(), any(), any()) } throws RuntimeException("FCM down")

        val result = useCase.invoke("team_fs_1", "coach1")

        assertEquals("coach1", result.coachId)
        coVerify { teamRepository.updateTeamCoachId("team_fs_1", "coach1") }
    }

    @Test
    fun `givenCoachAlreadyHasCoachRole_whenInvoke_thenDoNotAddRoleAgain`() = runTest {
        val coachWithRole = coachMember.copy(roles = listOf(ClubRole.COACH.roleName))
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(presidentMember)
        coEvery { clubMemberRepository.getClubMemberByUserIdAndClub("coach1", "club_fs_1") } returns coachWithRole

        useCase.invoke("team_fs_1", "coach1")

        coVerify(exactly = 0) { clubMemberRepository.addClubMemberRole(any(), any(), any()) }
    }
}
