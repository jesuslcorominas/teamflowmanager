package com.jesuslcorominas.teamflowmanager.data.core.repository

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
    private lateinit var repository: TeamRepositoryImpl

    @Before
    fun setup() {
        teamDataSource = mockk(relaxed = true)
        repository = TeamRepositoryImpl(teamDataSource)
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
}
