package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Team
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
    private lateinit var localDataSource: TeamLocalDataSource
    private lateinit var repository: TeamRepositoryImpl

    @Before
    fun setup() {
        localDataSource = mockk(relaxed = true)
        repository = TeamRepositoryImpl(localDataSource)
    }

    @Test
    fun `getTeam should return team from local data source`() =
        runTest {
            // Given
            val team = Team(1, "Test Team", "Coach Name", "Delegate Name")
            every { localDataSource.getTeam() } returns flowOf(team)

            // When
            val result = repository.getTeam().first()

            // Then
            assertEquals(team, result)
            verify { localDataSource.getTeam() }
        }

    @Test
    fun `getTeam should return null when no team exists`() =
        runTest {
            // Given
            every { localDataSource.getTeam() } returns flowOf(null)

            // When
            val result = repository.getTeam().first()

            // Then
            assertNull(result)
            verify { localDataSource.getTeam() }
        }

    @Test
    fun `createTeam should call insertTeam on local data source`() =
        runTest {
            // Given
            val team = Team(0, "Test Team", "Coach Name", "Delegate Name")
            coEvery { localDataSource.insertTeam(team) } just runs

            // When
            repository.createTeam(team)

            // Then
            coVerify { localDataSource.insertTeam(team) }
        }

    @Test
    fun `updateTeam should call updateTeam on local data source`() =
        runTest {
            // Given
            val team = Team(1, "Updated Team", "New Coach", "New Delegate")
            coEvery { localDataSource.updateTeam(team) } just runs

            // When
            repository.updateTeam(team)

            // Then
            coVerify { localDataSource.updateTeam(team) }
        }
}
