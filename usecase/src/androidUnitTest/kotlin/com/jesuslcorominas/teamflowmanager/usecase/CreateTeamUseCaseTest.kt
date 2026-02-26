package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateTeamUseCaseTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var createTeamUseCase: CreateTeamUseCase

    @Before
    fun setup() {
        teamRepository = mockk(relaxed = true)
        createTeamUseCase = CreateTeamUseCaseImpl(teamRepository)
    }

    @Test
    fun `invoke should create team in repository`() =
        runTest {
            // Given
            val team = Team(0, "Test Team", "Coach Name", "Delegate Name", teamType = TeamType.FOOTBALL_5)

            // When
            createTeamUseCase.invoke(team)

            // Then
            coVerify { teamRepository.createTeam(team) }
        }
}
