package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SkeletonMatch
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CreateMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var teamRepository: TeamRepository
    private lateinit var createMatchUseCase: CreateMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        teamRepository = mockk(relaxed = true)
        createMatchUseCase = CreateMatchUseCaseImpl(matchRepository, teamRepository)
    }

    @Test
    fun `invoke should create match in repository and return id`() =
        runTest {
            // Given
            val team = Team(id = 1L, name = "Team A", coachName = "Coach", delegateName = "Delegate", teamType = TeamType.FOOTBALL_7)
            val skeleton = SkeletonMatch(
                opponent = "Rival FC",
                location = "Stadium",
                dateTime = null,
                numberOfPeriods = 2,
                squadCallUpIds = listOf(1L, 2L, 3L),
                captainId = 1L,
                startingLineupIds = listOf(1L, 2L),
            )
            val expectedId = 123L
            coEvery { teamRepository.getTeam() } returns flowOf(team)
            coEvery { matchRepository.createMatch(any()) } returns expectedId

            // When
            val result = createMatchUseCase.invoke(skeleton)

            // Then
            assertEquals(expectedId, result)
            coVerify { matchRepository.createMatch(any()) }
        }

    @Test(expected = IllegalStateException::class)
    fun `invoke should throw exception when no team exists`() =
        runTest {
            // Given
            val skeleton = SkeletonMatch(
                opponent = "Rival FC",
                location = "Stadium",
                dateTime = null,
                numberOfPeriods = 2,
                squadCallUpIds = emptyList(),
                captainId = 1L,
                startingLineupIds = emptyList(),
            )
            coEvery { teamRepository.getTeam() } returns flowOf(null)

            // When - should throw IllegalStateException
            createMatchUseCase.invoke(skeleton)
        }
}
