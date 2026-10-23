package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.SkeletonMatch
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
            val team = Team(id = "1", name = "Team A", coachName = "Coach", delegateName = "Delegate", teamType = TeamType.FOOTBALL_7)
            val skeleton = SkeletonMatch(
                opponent = "Rival FC",
                location = "Stadium",
                dateTime = null,
                numberOfPeriods = 2,
                squadCallUpIds = listOf("1", "2", "3"),
                captainId = "1",
                startingLineupIds = listOf("1", "2"),
            )
            val expectedId = "123"
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
                captainId = "1",
                startingLineupIds = emptyList(),
            )
            coEvery { teamRepository.getTeam() } returns flowOf(null)

            // When - should throw IllegalStateException
            createMatchUseCase.invoke(skeleton)
        }

    private suspend fun createAndCapture(
        teamType: TeamType,
        numberOfPeriods: Int,
    ): Match {
        val team = Team(id = "1", name = "Team A", coachName = "Coach", delegateName = "Delegate", teamType = teamType)
        val skeleton = SkeletonMatch(
            opponent = "Rival FC",
            location = "Stadium",
            dateTime = null,
            numberOfPeriods = numberOfPeriods,
            squadCallUpIds = emptyList(),
            captainId = "1",
            startingLineupIds = emptyList(),
        )
        val matchSlot = slot<Match>()
        coEvery { teamRepository.getTeam() } returns flowOf(team)
        coEvery { matchRepository.createMatch(capture(matchSlot)) } returns "123"

        createMatchUseCase.invoke(skeleton)

        return matchSlot.captured
    }

    @Test
    fun `invoke with FOOTBALL_7 team creates 2 periods of 30 minutes`() =
        runTest {
            // When
            val match = createAndCapture(TeamType.FOOTBALL_7, numberOfPeriods = 2)

            // Then
            assertEquals(PeriodType.HALF_TIME, match.periodType)
            assertEquals(2, match.periods.size)
            assertTrue(match.periods.all { it.periodDuration == 1_800_000L })
        }

    @Test
    fun `invoke with FOOTBALL_11 team creates 2 periods of 45 minutes`() =
        runTest {
            // When
            val match = createAndCapture(TeamType.FOOTBALL_11, numberOfPeriods = 2)

            // Then
            assertEquals(2, match.periods.size)
            assertTrue(match.periods.all { it.periodDuration == 2_700_000L })
        }

    @Test
    fun `invoke with FOOTBALL_5 team and 4 periods creates 4 periods of 12 minutes 30 seconds`() =
        runTest {
            // When
            val match = createAndCapture(TeamType.FOOTBALL_5, numberOfPeriods = 4)

            // Then
            assertEquals(PeriodType.QUARTER_TIME, match.periodType)
            assertEquals(4, match.periods.size)
            assertTrue(match.periods.all { it.periodDuration == 750_000L })
        }

    @Test
    fun `invoke with FOOTBALL_8 team forces 2 periods of 30 minutes when skeleton asks for 4`() =
        runTest {
            // When
            val match = createAndCapture(TeamType.FOOTBALL_8, numberOfPeriods = 4)

            // Then
            assertEquals(PeriodType.HALF_TIME, match.periodType)
            assertEquals(2, match.periods.size)
            assertTrue(match.periods.all { it.periodDuration == 1_800_000L })
        }
}
