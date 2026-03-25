package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetTeamUseCaseTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var getTeamUseCase: GetTeamUseCase

    @Before
    fun setup() {
        teamRepository = mockk(relaxed = true)
        getTeamUseCase = GetTeamUseCaseImpl(teamRepository)
    }

    @Test
    fun `invoke should return team from repository`() =
        runTest {
            // Given
            val team = Team(1, "Test Team", "Coach Name", "Delegate Name", teamType = TeamType.FOOTBALL_5)
            every { teamRepository.getTeam() } returns flowOf(team)

            // When
            val result = getTeamUseCase.invoke().first()

            // Then
            assertEquals(team, result)
            verify { teamRepository.getTeam() }
        }

    @Test
    fun `invoke should return null when no team exists`() =
        runTest {
            // Given
            every { teamRepository.getTeam() } returns flowOf(null)

            // When
            val result = getTeamUseCase.invoke().first()

            // Then
            assertNull(result)
            verify { teamRepository.getTeam() }
        }
}
