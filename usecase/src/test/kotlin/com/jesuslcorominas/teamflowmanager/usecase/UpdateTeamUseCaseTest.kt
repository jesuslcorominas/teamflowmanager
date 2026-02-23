package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateTeamUseCaseTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var updateTeamUseCase: UpdateTeamUseCase

    @Before
    fun setup() {
        teamRepository = mockk(relaxed = true)
        updateTeamUseCase = UpdateTeamUseCaseImpl(teamRepository)
    }

    @Test
    fun `invoke should call repository updateTeam with correct team`() =
        runTest {
            // Given
            val team = Team(1, "Updated Team", "Updated Coach", "Updated Delegate", teamType = TeamType.FOOTBALL_5)

            // When
            updateTeamUseCase.invoke(team)

            // Then
            coVerify { teamRepository.updateTeam(team) }
        }
}
