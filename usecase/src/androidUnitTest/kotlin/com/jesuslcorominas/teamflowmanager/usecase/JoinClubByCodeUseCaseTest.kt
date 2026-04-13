package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubByCodeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentOnMemberWaitingUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class JoinClubByCodeUseCaseTest {
    private lateinit var clubRepository: ClubRepository
    private lateinit var teamRepository: TeamRepository
    private lateinit var clubMemberRepository: ClubMemberRepository
    private lateinit var getCurrentUser: GetCurrentUserUseCase
    private lateinit var notifyPresidentOnMemberWaiting: NotifyPresidentOnMemberWaitingUseCase
    private lateinit var useCase: JoinClubByCodeUseCase

    private val authenticatedUser = User(id = "user1", email = "alice@test.com", displayName = "Alice", photoUrl = null)
    private val club = Club(id = 10L, ownerId = "owner1", name = "Club A", invitationCode = "ABC123", remoteId = "club_fs_1")
    private val clubMember = ClubMember(id = 1L, userId = "user1", name = "Alice", email = "alice@test.com", clubId = 10L, roles = listOf("Staff"))

    @Before
    fun setup() {
        clubRepository = mockk()
        teamRepository = mockk(relaxed = true)
        clubMemberRepository = mockk()
        getCurrentUser = mockk()
        notifyPresidentOnMemberWaiting = mockk(relaxed = true)
        useCase = JoinClubByCodeUseCaseImpl(clubRepository, teamRepository, clubMemberRepository, getCurrentUser, notifyPresidentOnMemberWaiting)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenBlankInvitationCode_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        useCase.invoke("")
    }

    @Test(expected = IllegalStateException::class)
    fun `givenUnauthenticatedUser_whenInvoke_thenThrowIllegalStateException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(null)
        useCase.invoke("ABC123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenClubNotFound_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(authenticatedUser)
        coEvery { clubRepository.getClubByInvitationCode("INVALID") } returns null
        useCase.invoke("INVALID")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenUserWithNullDisplayName_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val userWithNullName = authenticatedUser.copy(displayName = null)
        coEvery { getCurrentUser() } returns flowOf(userWithNullName)
        useCase.invoke("ABC123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenUserWithBlankDisplayName_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val userWithBlankName = authenticatedUser.copy(displayName = "")
        coEvery { getCurrentUser() } returns flowOf(userWithBlankName)
        useCase.invoke("ABC123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenUserWithNullEmail_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val userWithNullEmail = authenticatedUser.copy(email = null)
        coEvery { getCurrentUser() } returns flowOf(userWithNullEmail)
        useCase.invoke("ABC123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenUserWithBlankEmail_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val userWithBlankEmail = authenticatedUser.copy(email = "")
        coEvery { getCurrentUser() } returns flowOf(userWithBlankEmail)
        useCase.invoke("ABC123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenClubWithNullFirestoreId_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val clubWithNullFirestoreId = club.copy(remoteId = null)
        coEvery { getCurrentUser() } returns flowOf(authenticatedUser)
        coEvery { clubRepository.getClubByInvitationCode("ABC123") } returns clubWithNullFirestoreId
        useCase.invoke("ABC123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `givenOrphanTeamWithNullFirestoreId_whenInvoke_thenThrowIllegalArgumentException`() = runTest {
        val orphanTeamWithNullFirestoreId = Team(
            id = 5L, name = "My Team", coachName = "Coach", delegateName = "Del",
            teamType = TeamType.FOOTBALL_7, remoteId = null,
        )
        coEvery { getCurrentUser() } returns flowOf(authenticatedUser)
        coEvery { clubRepository.getClubByInvitationCode("ABC123") } returns club
        coEvery { teamRepository.getOrphanTeams("user1") } returns listOf(orphanTeamWithNullFirestoreId)
        useCase.invoke("ABC123")
    }

    @Test
    fun `givenValidCodeAndNoOrphanTeam_whenInvoke_thenJoinWithStaffRole`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(authenticatedUser)
        coEvery { clubRepository.getClubByInvitationCode("ABC123") } returns club
        coEvery { teamRepository.getOrphanTeams("user1") } returns emptyList()
        coEvery { clubMemberRepository.createOrUpdateClubMember(any(), any(), any(), any(), any(), any()) } returns clubMember

        val result = useCase.invoke("ABC123")

        assertEquals(club, result.club)
        assertNull(result.orphanTeam)
        assertEquals(clubMember, result.clubMember)
        coVerify { clubMemberRepository.createOrUpdateClubMember("user1", "Alice", "alice@test.com", 10L, "club_fs_1", listOf("Staff")) }
    }

    @Test
    fun `givenValidCodeWithOrphanTeam_whenInvoke_thenJoinWithCoachRoleAndLinkTeam`() = runTest {
        val orphanTeam = Team(id = 5L, name = "My Team", coachName = "Coach", delegateName = "Del", teamType = TeamType.FOOTBALL_7, remoteId = "team_fs_1")
        val memberWithCoach = ClubMember(id = 1L, userId = "user1", name = "Alice", email = "alice@test.com", clubId = 10L, roles = listOf("Coach"))

        coEvery { getCurrentUser() } returns flowOf(authenticatedUser)
        coEvery { clubRepository.getClubByInvitationCode("ABC123") } returns club
        coEvery { teamRepository.getOrphanTeams("user1") } returns listOf(orphanTeam)
        coEvery { clubMemberRepository.createOrUpdateClubMember(any(), any(), any(), any(), any(), any()) } returns memberWithCoach

        val result = useCase.invoke("ABC123")

        assertEquals(orphanTeam, result.orphanTeam)
        coVerify { teamRepository.updateTeamClubId("team_fs_1", 10L, "club_fs_1") }
        coVerify { clubMemberRepository.createOrUpdateClubMember("user1", "Alice", "alice@test.com", 10L, "club_fs_1", listOf("Coach")) }
    }

    @Test
    fun `givenValidCodeAndNoOrphanTeam_whenInvoke_thenNotifiesPresident`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(authenticatedUser)
        coEvery { clubRepository.getClubByInvitationCode("ABC123") } returns club
        coEvery { teamRepository.getOrphanTeams("user1") } returns emptyList()
        coEvery { clubMemberRepository.createOrUpdateClubMember(any(), any(), any(), any(), any(), any()) } returns clubMember

        useCase.invoke("ABC123")

        coVerify {
            notifyPresidentOnMemberWaiting(
                clubId = "club_fs_1",
                presidentUserId = "owner1",
                userName = "Alice",
                userEmail = "alice@test.com",
            )
        }
    }

    @Test
    fun `givenValidCodeWithOrphanTeam_whenInvoke_thenDoesNotNotifyPresident`() = runTest {
        val orphanTeam = Team(id = 5L, name = "My Team", coachName = "Coach", delegateName = "Del", teamType = TeamType.FOOTBALL_7, remoteId = "team_fs_1")
        val memberWithCoach = ClubMember(id = 1L, userId = "user1", name = "Alice", email = "alice@test.com", clubId = 10L, roles = listOf("Coach"))

        coEvery { getCurrentUser() } returns flowOf(authenticatedUser)
        coEvery { clubRepository.getClubByInvitationCode("ABC123") } returns club
        coEvery { teamRepository.getOrphanTeams("user1") } returns listOf(orphanTeam)
        coEvery { clubMemberRepository.createOrUpdateClubMember(any(), any(), any(), any(), any(), any()) } returns memberWithCoach

        useCase.invoke("ABC123")

        coVerify(exactly = 0) { notifyPresidentOnMemberWaiting(any(), any(), any(), any()) }
    }

    @Test
    fun `givenNotifyPresidentThrows_whenInvoke_thenJoinSucceeds`() = runTest {
        coEvery { getCurrentUser() } returns flowOf(authenticatedUser)
        coEvery { clubRepository.getClubByInvitationCode("ABC123") } returns club
        coEvery { teamRepository.getOrphanTeams("user1") } returns emptyList()
        coEvery { clubMemberRepository.createOrUpdateClubMember(any(), any(), any(), any(), any(), any()) } returns clubMember
        coEvery { notifyPresidentOnMemberWaiting(any(), any(), any(), any()) } throws RuntimeException("Notification failed")

        val result = useCase.invoke("ABC123")

        assertEquals(club, result.club)
        assertEquals(clubMember, result.clubMember)
        assertNull(result.orphanTeam)
    }
}
