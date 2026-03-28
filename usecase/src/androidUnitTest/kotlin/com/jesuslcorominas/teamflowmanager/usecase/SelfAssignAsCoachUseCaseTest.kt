package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SelfAssignAsCoachUseCase
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

class SelfAssignAsCoachUseCaseTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var clubMemberRepository: ClubMemberRepository
    private lateinit var getCurrentUser: GetCurrentUserUseCase
    private lateinit var useCase: SelfAssignAsCoachUseCase

    private val president = User(id = "user1", email = "pres@test.com", displayName = "President", photoUrl = null)
    private val team = Team(
        id = 1L,
        name = "Team A",
        coachName = "",
        delegateName = "Del",
        teamType = TeamType.FOOTBALL_7,
        firestoreId = "team_fs_1",
        clubFirestoreId = "club_fs_1",
        clubId = 10L,
        coachId = null,
    )
    private val presidentMember = ClubMember(
        id = 1L,
        userId = "user1",
        name = "President",
        email = "pres@test.com",
        clubId = 10L,
        roles = listOf(ClubRole.PRESIDENT.roleName),
        clubFirestoreId = "club_fs_1",
    )

    @Before
    fun setup() {
        teamRepository = mockk(relaxed = true)
        clubMemberRepository = mockk(relaxed = true)
        getCurrentUser = mockk()
        useCase = SelfAssignAsCoachUseCaseImpl(teamRepository, clubMemberRepository, getCurrentUser)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenBlankTeamFirestoreId_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        useCase.invoke("")
    }

    @Test(expected = IllegalStateException::class)
    fun `givenUnauthenticatedUser_whenInvoke_thenThrowIllegalStateException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(null)
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenTeamNotFound_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns null
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenTeamNotInClub_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val teamWithoutClub = team.copy(clubFirestoreId = null)
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns teamWithoutClub
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenTeamAlreadyHasCoach_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val teamWithCoach = team.copy(coachId = "existingCoach")
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns teamWithCoach
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalStateException::class)
    fun `givenUserNotClubMember_whenInvoke_thenThrowIllegalStateException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(null)
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenNonPresidentUser_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val staffMember = presidentMember.copy(roles = listOf(ClubRole.STAFF.roleName))
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(staffMember)
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenPresidentFromDifferentClub_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val differentClubMember = presidentMember.copy(clubFirestoreId = "other_club_fs")
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(differentClubMember)
        useCase.invoke("team_fs_1")
    }

    @Test
    fun `givenPresidentWithoutCoachRole_whenInvoke_thenAssignCoachAndAddCoachRole`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(presidentMember)

        val result = useCase.invoke("team_fs_1")

        assertEquals("user1", result.coachId)
        coVerify { teamRepository.updateTeamCoachId("team_fs_1", "user1") }
        coVerify { clubMemberRepository.addClubMemberRole("user1", "club_fs_1", ClubRole.COACH.roleName) }
    }

    @Test
    fun `givenPresidentAlreadyHasCoachRole_whenInvoke_thenAssignCoachWithoutAddingRoleAgain`() = runTest {
        val presidentWithCoachRole = presidentMember.copy(
            roles = listOf(ClubRole.PRESIDENT.roleName, ClubRole.COACH.roleName)
        )
        coEvery { getCurrentUser() } returns flowOf(president)
        coEvery { teamRepository.getTeamById("team_fs_1") } returns team
        coEvery { clubMemberRepository.getClubMemberByUserId("user1") } returns flowOf(presidentWithCoachRole)

        useCase.invoke("team_fs_1")

        coVerify { teamRepository.updateTeamCoachId("team_fs_1", "user1") }
        coVerify(exactly = 0) { clubMemberRepository.addClubMemberRole(any(), any(), any()) }
    }
}
