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
import io.mockk.verify
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

    @Test
    fun `getTeam should return team from local data source`() =
        runTest {
            // Given
            val team = Team(1, "Test Team", "Coach Name", "Delegate Name", teamType = TeamType.FOOTBALL_5)
            every { teamDataSource.getTeam() } returns flowOf(team)

            // When
            val result = repository.getTeam().first()

            // Then
            assertEquals(team, result)
            verify { teamDataSource.getTeam() }
        }

    @Test
    fun `getTeam should return null when no team exists`() =
        runTest {
            // Given
            every { teamDataSource.getTeam() } returns flowOf(null)

            // When
            val result = repository.getTeam().first()

            // Then
            assertNull(result)
            verify { teamDataSource.getTeam() }
        }

    @Test
    fun `createTeam should call insertTeam on local data source`() =
        runTest {
            // Given
            val team = Team(0, "Test Team", "Coach Name", "Delegate Name", teamType = TeamType.FOOTBALL_5)
            coEvery { teamDataSource.insertTeam(team) } just runs

            // When
            repository.createTeam(team)

            // Then
            coVerify { teamDataSource.insertTeam(team) }
        }

    @Test
    fun `updateTeam should call updateTeam on local data source`() =
        runTest {
            // Given
            val team = Team(1, "Updated Team", "New Coach", "New Delegate", teamType = TeamType.FOOTBALL_5)
            coEvery { teamDataSource.updateTeam(team) } just runs

            // When
            repository.updateTeam(team)

            // Then
            coVerify { teamDataSource.updateTeam(team) }
        }

    @Test
    fun `getTeamByCoachId should return team from local data source`() =
        runTest {
            // Given
            val coachId = "test-coach-id"
            val team = Team(1, "Test Team", "Coach Name", "Delegate Name", teamType = TeamType.FOOTBALL_5, coachId = coachId)
            every { teamDataSource.getTeamByCoachId(coachId) } returns flowOf(team)

            // When
            val result = repository.getTeamByCoachId(coachId).first()

            // Then
            assertEquals(team, result)
            verify { teamDataSource.getTeamByCoachId(coachId) }
        }

    @Test
    fun `getTeamByCoachId should return null when no team exists for coach`() =
        runTest {
            // Given
            val coachId = "non-existent-coach-id"
            every { teamDataSource.getTeamByCoachId(coachId) } returns flowOf(null)

            // When
            val result = repository.getTeamByCoachId(coachId).first()

            // Then
            assertNull(result)
            verify { teamDataSource.getTeamByCoachId(coachId) }
        }

    @Test
    fun `hasLocalTeamWithoutUserId should return true when team without coachId exists`() =
        runTest {
            // Given
            coEvery { teamDataSource.hasLocalTeamWithoutUserId() } returns true

            // When
            val result = repository.hasLocalTeamWithoutUserId()

            // Then
            assertEquals(true, result)
            coVerify { teamDataSource.hasLocalTeamWithoutUserId() }
        }

    @Test
    fun `hasLocalTeamWithoutUserId should return false when no team without coachId exists`() =
        runTest {
            // Given
            coEvery { teamDataSource.hasLocalTeamWithoutUserId() } returns false

            // When
            val result = repository.hasLocalTeamWithoutUserId()

            // Then
            assertEquals(false, result)
            coVerify { teamDataSource.hasLocalTeamWithoutUserId() }
        }

    @Test
    fun `createTeam should handle team with clubId`() =
        runTest {
            // Given
            val team = Team(
                id = 0,
                name = "Test Team",
                coachName = "Coach Name",
                delegateName = "Delegate Name",
                teamType = TeamType.FOOTBALL_5,
                clubId = 123L,
                clubFirestoreId = "club_abc123xyz",
            )
            coEvery { teamDataSource.insertTeam(team) } just runs

            // When
            repository.createTeam(team)

            // Then
            coVerify { teamDataSource.insertTeam(team) }
        }

    @Test
    fun `createTeam should handle orphaned team with null clubId`() =
        runTest {
            // Given
            val team = Team(
                id = 0,
                name = "Orphaned Team",
                coachName = "Coach Name",
                delegateName = "Delegate Name",
                teamType = TeamType.FOOTBALL_5,
                clubId = null,
                clubFirestoreId = null,
            )
            coEvery { teamDataSource.insertTeam(team) } just runs

            // When
            repository.createTeam(team)

            // Then
            coVerify { teamDataSource.insertTeam(team) }
        }

    @Test
    fun `updateTeam should handle team with clubId`() =
        runTest {
            // Given
            val team = Team(
                id = 1,
                name = "Updated Team",
                coachName = "New Coach",
                delegateName = "New Delegate",
                teamType = TeamType.FOOTBALL_5,
                clubId = 456L,
                clubFirestoreId = "club_def456uvw",
            )
            coEvery { teamDataSource.updateTeam(team) } just runs

            // When
            repository.updateTeam(team)

            // Then
            coVerify { teamDataSource.updateTeam(team) }
        }

    @Test
    fun `getTeam should return team with clubId`() =
        runTest {
            // Given
            val team = Team(
                id = 1,
                name = "Test Team",
                coachName = "Coach Name",
                delegateName = "Delegate Name",
                teamType = TeamType.FOOTBALL_5,
                clubId = 789L,
                clubFirestoreId = "club_ghi789rst",
            )
            every { teamDataSource.getTeam() } returns flowOf(team)

            // When
            val result = repository.getTeam().first()

            // Then
            assertEquals(team, result)
            assertEquals(789L, result?.clubId)
            assertEquals("club_ghi789rst", result?.clubFirestoreId)
            verify { teamDataSource.getTeam() }
        }
}
