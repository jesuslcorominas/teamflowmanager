package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateTeamUseCaseTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var getCurrentUser: GetCurrentUserUseCase
    private lateinit var getUserClubMembership: GetUserClubMembershipUseCase
    private lateinit var createTeamUseCase: CreateTeamUseCase

    private val currentUser = User(id = "user123", email = "user@test.com", displayName = "User", photoUrl = null)
    private val baseTeam = Team(0, "Test Team", "Coach Name", "Delegate Name", teamType = TeamType.FOOTBALL_5)

    private val presidentMember = ClubMember(
        id = 1L,
        userId = "user123",
        name = "President",
        email = "user@test.com",
        clubId = 10L,
        roles = listOf(ClubRole.PRESIDENT.roleName),
        clubFirestoreId = "club_fs_1",
    )

    @Before
    fun setup() {
        teamRepository = mockk(relaxed = true)
        getCurrentUser = mockk()
        getUserClubMembership = mockk()
    }

    private fun buildUseCase() = CreateTeamUseCaseImpl(teamRepository, getCurrentUser, getUserClubMembership)

    @Test
    fun `given user without club, when create team, then coachId is set to current user id`() = runTest {
        coEvery { getUserClubMembership() } returns flowOf(null)
        coEvery { getCurrentUser() } returns flowOf(currentUser)

        createTeamUseCase = buildUseCase()
        createTeamUseCase.invoke(baseTeam)

        coVerify { teamRepository.createTeam(baseTeam.copy(coachId = "user123")) }
    }

    @Test
    fun `given coach role user, when create team, then coachId is set to current user id`() = runTest {
        val coachMember = presidentMember.copy(roles = listOf(ClubRole.COACH.roleName))
        coEvery { getUserClubMembership() } returns flowOf(coachMember)
        coEvery { getCurrentUser() } returns flowOf(currentUser)

        createTeamUseCase = buildUseCase()
        createTeamUseCase.invoke(baseTeam)

        coVerify { teamRepository.createTeam(baseTeam.copy(coachId = "user123")) }
    }

    @Test
    fun `given president user, when create team, then coachId is left null`() = runTest {
        coEvery { getUserClubMembership() } returns flowOf(presidentMember)

        createTeamUseCase = buildUseCase()
        createTeamUseCase.invoke(baseTeam)

        coVerify { teamRepository.createTeam(baseTeam) }
    }

    @Test
    fun `given team already has coachId, when create team, then coachId is not overridden`() = runTest {
        val teamWithCoach = baseTeam.copy(coachId = "existingCoach")
        coEvery { getUserClubMembership() } returns flowOf(null)

        createTeamUseCase = buildUseCase()
        createTeamUseCase.invoke(teamWithCoach)

        coVerify { teamRepository.createTeam(teamWithCoach) }
    }

    @Test
    fun `given unauthenticated user without club, when create team, then coachId stays null`() = runTest {
        coEvery { getUserClubMembership() } returns flowOf(null)
        coEvery { getCurrentUser() } returns flowOf(null)

        createTeamUseCase = buildUseCase()
        createTeamUseCase.invoke(baseTeam)

        coVerify { teamRepository.createTeam(baseTeam) }
    }
}