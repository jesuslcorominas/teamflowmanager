package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamsByClubUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTeamsByClubUseCaseTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var useCase: GetTeamsByClubUseCase

    @Before
    fun setup() {
        teamRepository = mockk()
        useCase = GetTeamsByClubUseCaseImpl(teamRepository)
    }

    @Test
    fun `invoke should return teams for given club`() = runTest {
        val clubId = "club123"
        val teams = listOf(
            Team(id = 1L, name = "Team A", coachName = "Coach A", delegateName = "Delegate A", teamType = TeamType.FOOTBALL_7),
            Team(id = 2L, name = "Team B", coachName = "Coach B", delegateName = "Delegate B", teamType = TeamType.FOOTBALL_11),
        )
        every { teamRepository.getTeamsByClub(clubId) } returns flowOf(teams)

        val result = useCase.invoke(clubId).first()

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
    }

    @Test
    fun `invoke should return empty list when club has no teams`() = runTest {
        val clubId = "emptyClub"
        every { teamRepository.getTeamsByClub(clubId) } returns flowOf(emptyList())

        val result = useCase.invoke(clubId).first()

        assertTrue(result.isEmpty())
    }
}
