package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.AcceptTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
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

class AcceptTeamInvitationUseCaseTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var clubMemberRepository: ClubMemberRepository
    private lateinit var getCurrentUser: GetCurrentUserUseCase
    private lateinit var useCase: AcceptTeamInvitationUseCase

    private val coach = User(id = "coach1", email = "coach@test.com", displayName = "Coach Name", photoUrl = null)
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

    @Before
    fun setup() {
        teamRepository = mockk(relaxed = true)
        clubMemberRepository = mockk(relaxed = true)
        getCurrentUser = mockk()
        useCase = AcceptTeamInvitationUseCaseImpl(teamRepository, clubMemberRepository, getCurrentUser)
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
    fun `givenUserWithBlankDisplayName_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val userWithBlankName = coach.copy(displayName = "")
        coEvery { getCurrentUser() } returns flowOf(userWithBlankName)
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenUserWithNullDisplayName_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val userWithNullName = coach.copy(displayName = null)
        coEvery { getCurrentUser() } returns flowOf(userWithNullName)
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenUserWithBlankEmail_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val userWithBlankEmail = coach.copy(email = "")
        coEvery { getCurrentUser() } returns flowOf(userWithBlankEmail)
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenUserWithNullEmail_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val userWithNullEmail = coach.copy(email = null)
        coEvery { getCurrentUser() } returns flowOf(userWithNullEmail)
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenTeamNotFound_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(coach)
        coEvery { teamRepository.getTeamByFirestoreId("team_fs_1") } returns null
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenTeamAlreadyHasCoach_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val teamWithCoach = team.copy(coachId = "existingCoach")
        coEvery { getCurrentUser() } returns flowOf(coach)
        coEvery { teamRepository.getTeamByFirestoreId("team_fs_1") } returns teamWithCoach
        useCase.invoke("team_fs_1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenTeamNotInClub_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val teamWithoutClub = team.copy(clubFirestoreId = null, clubId = null)
        coEvery { getCurrentUser() } returns flowOf(coach)
        coEvery { teamRepository.getTeamByFirestoreId("team_fs_1") } returns teamWithoutClub
        useCase.invoke("team_fs_1")
    }

    @Test
    fun `givenValidCoachAndTeam_whenInvoke_thenUpdateTeamCoachIdAndCreateMember`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(coach)
        coEvery { teamRepository.getTeamByFirestoreId("team_fs_1") } returns team
        coEvery { clubMemberRepository.createOrUpdateClubMember(any(), any(), any(), any(), any(), any()) } returns mockk()

        val result = useCase.invoke("team_fs_1")

        assertEquals("coach1", result.coachId)
        coVerify { teamRepository.updateTeamCoachId("team_fs_1", "coach1") }
        coVerify {
            clubMemberRepository.createOrUpdateClubMember(
                "coach1", "Coach Name", "coach@test.com", 10L, "club_fs_1", listOf(ClubRole.COACH.roleName)
            )
        }
    }
}
