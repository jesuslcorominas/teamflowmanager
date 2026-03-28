package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.DynamicLinkDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TeamRepositoryImplTest {

    private lateinit var teamDataSource: TeamDataSource
    private lateinit var dynamicLinkDataSource: DynamicLinkDataSource
    private lateinit var repository: TeamRepositoryImpl

    @Before
    fun setup() {
        teamDataSource = mockk(relaxed = true)
        dynamicLinkDataSource = mockk(relaxed = true)
        repository = TeamRepositoryImpl(teamDataSource, dynamicLinkDataSource)
    }

    private fun createTeam(
        id: Long = 1L,
        name: String = "Test Team",
        coachId: String? = null,
        clubId: Long? = null,
        clubFirestoreId: String? = null,
        firestoreId: String? = null,
    ) = Team(
        id = id,
        name = name,
        coachName = "Coach Name",
        delegateName = "Delegate Name",
        teamType = TeamType.FOOTBALL_5,
        coachId = coachId,
        clubId = clubId,
        clubFirestoreId = clubFirestoreId,
        firestoreId = firestoreId,
    )

    // --- getTeam ---

    @Test
    fun `givenExistingTeam_whenGetTeam_thenDelegatesToDataSource`() = runTest {
        val team = createTeam()
        every { teamDataSource.getTeam() } returns flowOf(team)

        val result = repository.getTeam().first()

        assertEquals(team, result)
    }

    @Test
    fun `givenNoTeam_whenGetTeam_thenReturnsNull`() = runTest {
        every { teamDataSource.getTeam() } returns flowOf(null)

        val result = repository.getTeam().first()

        assertNull(result)
    }

    // --- createTeam ---

    @Test
    fun `givenTeam_whenCreateTeam_thenDelegatesToDataSource`() = runTest {
        val team = createTeam(id = 0L)
        coEvery { teamDataSource.insertTeam(team) } just runs

        repository.createTeam(team)

        coVerify { teamDataSource.insertTeam(team) }
    }

    @Test
    fun `givenTeamWithClubId_whenCreateTeam_thenDelegatesToDataSource`() = runTest {
        val team = createTeam(id = 0L, clubId = 123L, clubFirestoreId = "club-abc")
        coEvery { teamDataSource.insertTeam(team) } just runs

        repository.createTeam(team)

        coVerify { teamDataSource.insertTeam(team) }
    }

    @Test
    fun `givenOrphanTeamWithNullClubId_whenCreateTeam_thenDelegatesToDataSource`() = runTest {
        val team = createTeam(id = 0L, clubId = null, clubFirestoreId = null)
        coEvery { teamDataSource.insertTeam(team) } just runs

        repository.createTeam(team)

        coVerify { teamDataSource.insertTeam(team) }
    }

    // --- updateTeam ---

    @Test
    fun `givenTeam_whenUpdateTeam_thenDelegatesToDataSource`() = runTest {
        val team = createTeam(id = 1L, name = "Updated Team")
        coEvery { teamDataSource.updateTeam(team) } just runs

        repository.updateTeam(team)

        coVerify { teamDataSource.updateTeam(team) }
    }

    // --- getTeamByCoachId ---

    @Test
    fun `givenCoachId_whenGetTeamByCoachId_thenReturnsTeam`() = runTest {
        val coachId = "coach-123"
        val team = createTeam(coachId = coachId)
        every { teamDataSource.getTeamByCoachId(coachId) } returns flowOf(team)

        val result = repository.getTeamByCoachId(coachId).first()

        assertEquals(team, result)
    }

    @Test
    fun `givenUnknownCoachId_whenGetTeamByCoachId_thenReturnsNull`() = runTest {
        val coachId = "unknown-coach"
        every { teamDataSource.getTeamByCoachId(coachId) } returns flowOf(null)

        val result = repository.getTeamByCoachId(coachId).first()

        assertNull(result)
    }

    // --- getTeamsByClub ---

    @Test
    fun `givenClubFirestoreId_whenGetTeamsByClub_thenReturnsTeams`() = runTest {
        val clubFirestoreId = "club-123"
        val teams = listOf(createTeam(id = 1L), createTeam(id = 2L))
        every { teamDataSource.getTeamsByClub(clubFirestoreId) } returns flowOf(teams)

        val result = repository.getTeamsByClub(clubFirestoreId).first()

        assertEquals(teams, result)
    }

    @Test
    fun `givenClubWithNoTeams_whenGetTeamsByClub_thenReturnsEmptyList`() = runTest {
        val clubFirestoreId = "empty-club"
        every { teamDataSource.getTeamsByClub(clubFirestoreId) } returns flowOf(emptyList())

        val result = repository.getTeamsByClub(clubFirestoreId).first()

        assertEquals(emptyList<Team>(), result)
    }

    // --- getOrphanTeams ---

    @Test
    fun `givenOwnerIdWithOrphanTeams_whenGetOrphanTeams_thenReturnsOrphanTeams`() = runTest {
        val ownerId = "owner-123"
        val orphanTeams = listOf(createTeam(id = 1L, clubId = null))
        coEvery { teamDataSource.getOrphanTeams(ownerId) } returns orphanTeams

        val result = repository.getOrphanTeams(ownerId)

        assertEquals(orphanTeams, result)
        coVerify { teamDataSource.getOrphanTeams(ownerId) }
    }

    @Test
    fun `givenOwnerIdWithNoOrphanTeams_whenGetOrphanTeams_thenReturnsEmptyList`() = runTest {
        val ownerId = "owner-456"
        coEvery { teamDataSource.getOrphanTeams(ownerId) } returns emptyList()

        val result = repository.getOrphanTeams(ownerId)

        assertEquals(emptyList<Team>(), result)
    }

    // --- updateTeamClubId ---

    @Test
    fun `givenTeamAndClubIds_whenUpdateTeamClubId_thenDelegatesToDataSource`() = runTest {
        repository.updateTeamClubId("firestore-team-id", 123L, "firestore-club-id")

        coVerify { teamDataSource.updateTeamClubId("firestore-team-id", 123L, "firestore-club-id") }
    }

    // --- getTeamById ---

    @Test
    fun `givenTeamFirestoreId_whenGetTeamByFirestoreId_thenReturnsTeam`() = runTest {
        val firestoreId = "firestore-team-123"
        val team = createTeam(firestoreId = firestoreId)
        coEvery { teamDataSource.getTeamById(firestoreId) } returns team

        val result = repository.getTeamById(firestoreId)

        assertEquals(team, result)
    }

    @Test
    fun `givenUnknownFirestoreId_whenGetTeamByFirestoreId_thenReturnsNull`() = runTest {
        coEvery { teamDataSource.getTeamById("unknown") } returns null

        val result = repository.getTeamById("unknown")

        assertNull(result)
    }

    // --- updateTeamCoachId ---

    @Test
    fun `givenTeamFirestoreIdAndCoachId_whenUpdateTeamCoachId_thenDelegatesToDataSource`() = runTest {
        repository.updateTeamCoachId("firestore-team-id", "new-coach-id")

        coVerify { teamDataSource.updateTeamCoachId("firestore-team-id", "new-coach-id") }
    }

    // --- generateTeamInvitationLink ---

    @Test
    fun `givenTeamFirestoreIdAndName_whenGenerateTeamInvitationLink_thenReturnsDynamicLink`() = runTest {
        val teamFirestoreId = "team-123"
        val teamName = "Test Team"
        val expectedLink = "https://example.page.link/team123"
        coEvery { dynamicLinkDataSource.generateTeamInvitationLink(teamFirestoreId, teamName) } returns expectedLink

        val result = repository.generateTeamInvitationLink(teamFirestoreId, teamName)

        assertEquals(expectedLink, result)
        coVerify { dynamicLinkDataSource.generateTeamInvitationLink(teamFirestoreId, teamName) }
    }
}
