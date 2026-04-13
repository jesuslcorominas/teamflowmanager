package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchesByTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersByTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamByIdUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PresidentTeamDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getTeamById: GetTeamByIdUseCase
    private lateinit var getPlayersByTeam: GetPlayersByTeamUseCase
    private lateinit var getMatchesByTeam: GetMatchesByTeamUseCase

    private val teamId = "team_fs_123"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamById = mockk()
        getPlayersByTeam = mockk()
        getMatchesByTeam = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        PresidentTeamDetailViewModel(
            teamId = teamId,
            getTeamById = getTeamById,
            getPlayersByTeam = getPlayersByTeam,
            getMatchesByTeam = getMatchesByTeam,
        )

    private fun aTeam() =
        Team(
            id = 1L,
            name = "FC Test",
            coachName = "Coach Name",
            delegateName = "Delegate",
            teamType = TeamType.FOOTBALL_5,
            remoteId = teamId,
        )

    private fun aPlayer(id: Long = 1L) =
        Player(
            id = id,
            firstName = "John",
            lastName = "Doe",
            number = id.toInt(),
            positions = listOf(Position.Forward),
            isCaptain = false,
            teamId = 1L,
        )

    private fun aMatch(
        id: Long = 1L,
        status: MatchStatus = MatchStatus.SCHEDULED,
        goals: Int = 0,
        opponentGoals: Int = 0,
        archived: Boolean = false,
        dateTime: Long? = id * 1000L,
    ) = Match(
        id = id,
        teamId = 1L,
        teamName = "FC Test",
        opponent = "Opponent $id",
        location = "Field",
        periodType = PeriodType.HALF_TIME,
        captainId = 0L,
        status = status,
        goals = goals,
        opponentGoals = opponentGoals,
        archived = archived,
        dateTime = dateTime,
    )

    @Test
    fun `initial state is Loading before load completes`() {
        coEvery { getTeamById(any()) } returns aTeam()
        every { getPlayersByTeam(any()) } returns flowOf(emptyList())
        every { getMatchesByTeam(any()) } returns flowOf(emptyList())

        val viewModel = createViewModel()

        assertEquals(PresidentTeamDetailUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `initial selected tab is SUMMARY`() {
        coEvery { getTeamById(any()) } returns aTeam()
        every { getPlayersByTeam(any()) } returns flowOf(emptyList())
        every { getMatchesByTeam(any()) } returns flowOf(emptyList())

        val viewModel = createViewModel()

        assertEquals(PresidentTeamTab.SUMMARY, viewModel.selectedTab.value)
    }

    @Test
    fun `when team is not found state becomes Error`() =
        runTest {
            coEvery { getTeamById(any()) } returns null

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(PresidentTeamDetailUiState.Error, viewModel.uiState.value)
        }

    @Test
    fun `when team found state becomes Ready with correct team`() =
        runTest {
            val team = aTeam()
            coEvery { getTeamById(any()) } returns team
            every { getPlayersByTeam(any()) } returns flowOf(emptyList())
            every { getMatchesByTeam(any()) } returns flowOf(emptyList())

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value as PresidentTeamDetailUiState.Ready
            assertEquals(team, state.team)
        }

    @Test
    fun `players are exposed in Ready state`() =
        runTest {
            val players = listOf(aPlayer(1L), aPlayer(2L))
            coEvery { getTeamById(any()) } returns aTeam()
            every { getPlayersByTeam(any()) } returns flowOf(players)
            every { getMatchesByTeam(any()) } returns flowOf(emptyList())

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value as PresidentTeamDetailUiState.Ready
            assertEquals(players, state.players)
        }

    @Test
    fun `squadSize in stats equals player count`() =
        runTest {
            coEvery { getTeamById(any()) } returns aTeam()
            every { getPlayersByTeam(any()) } returns flowOf(listOf(aPlayer(1L), aPlayer(2L), aPlayer(3L)))
            every { getMatchesByTeam(any()) } returns flowOf(emptyList())

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value as PresidentTeamDetailUiState.Ready
            assertEquals(3, state.stats.squadSize)
        }

    @Test
    fun `win is counted when goals greater than opponentGoals in finished match`() =
        runTest {
            val match = aMatch(status = MatchStatus.FINISHED, goals = 3, opponentGoals = 1)
            coEvery { getTeamById(any()) } returns aTeam()
            every { getPlayersByTeam(any()) } returns flowOf(emptyList())
            every { getMatchesByTeam(any()) } returns flowOf(listOf(match))

            val viewModel = createViewModel()
            advanceUntilIdle()

            val stats = (viewModel.uiState.value as PresidentTeamDetailUiState.Ready).stats
            assertEquals(1, stats.wins)
            assertEquals(0, stats.draws)
            assertEquals(0, stats.losses)
        }

    @Test
    fun `draw is counted when goals equal opponentGoals in finished match`() =
        runTest {
            val match = aMatch(status = MatchStatus.FINISHED, goals = 1, opponentGoals = 1)
            coEvery { getTeamById(any()) } returns aTeam()
            every { getPlayersByTeam(any()) } returns flowOf(emptyList())
            every { getMatchesByTeam(any()) } returns flowOf(listOf(match))

            val viewModel = createViewModel()
            advanceUntilIdle()

            val stats = (viewModel.uiState.value as PresidentTeamDetailUiState.Ready).stats
            assertEquals(0, stats.wins)
            assertEquals(1, stats.draws)
            assertEquals(0, stats.losses)
        }

    @Test
    fun `loss is counted when goals less than opponentGoals in finished match`() =
        runTest {
            val match = aMatch(status = MatchStatus.FINISHED, goals = 0, opponentGoals = 2)
            coEvery { getTeamById(any()) } returns aTeam()
            every { getPlayersByTeam(any()) } returns flowOf(emptyList())
            every { getMatchesByTeam(any()) } returns flowOf(listOf(match))

            val viewModel = createViewModel()
            advanceUntilIdle()

            val stats = (viewModel.uiState.value as PresidentTeamDetailUiState.Ready).stats
            assertEquals(0, stats.wins)
            assertEquals(0, stats.draws)
            assertEquals(1, stats.losses)
        }

    @Test
    fun `stats aggregate correctly across multiple finished matches`() =
        runTest {
            val matches =
                listOf(
                    aMatch(id = 1L, status = MatchStatus.FINISHED, goals = 3, opponentGoals = 1),
                    aMatch(id = 2L, status = MatchStatus.FINISHED, goals = 1, opponentGoals = 1),
                    aMatch(id = 3L, status = MatchStatus.FINISHED, goals = 0, opponentGoals = 2),
                )
            coEvery { getTeamById(any()) } returns aTeam()
            every { getPlayersByTeam(any()) } returns flowOf(emptyList())
            every { getMatchesByTeam(any()) } returns flowOf(matches)

            val viewModel = createViewModel()
            advanceUntilIdle()

            val stats = (viewModel.uiState.value as PresidentTeamDetailUiState.Ready).stats
            assertEquals(3, stats.totalMatches)
            assertEquals(1, stats.wins)
            assertEquals(1, stats.draws)
            assertEquals(1, stats.losses)
            assertEquals(4, stats.goalsScored)
            assertEquals(4, stats.goalsConceded)
        }

    @Test
    fun `scheduled match is not counted in stats but appears in matches list`() =
        runTest {
            val match = aMatch(id = 1L, status = MatchStatus.SCHEDULED)
            coEvery { getTeamById(any()) } returns aTeam()
            every { getPlayersByTeam(any()) } returns flowOf(emptyList())
            every { getMatchesByTeam(any()) } returns flowOf(listOf(match))

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value as PresidentTeamDetailUiState.Ready
            assertEquals(0, state.stats.totalMatches)
            assertEquals(1, state.matches.size)
        }

    @Test
    fun `archived matches are excluded from matches list`() =
        runTest {
            val active = aMatch(id = 1L, archived = false)
            val archived = aMatch(id = 2L, archived = true)
            coEvery { getTeamById(any()) } returns aTeam()
            every { getPlayersByTeam(any()) } returns flowOf(emptyList())
            every { getMatchesByTeam(any()) } returns flowOf(listOf(active, archived))

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value as PresidentTeamDetailUiState.Ready
            assertEquals(1, state.matches.size)
            assertEquals(1L, state.matches.first().id)
        }

    @Test
    fun `matches are sorted newest first by dateTime`() =
        runTest {
            val older = aMatch(id = 1L, dateTime = 1000L)
            val newer = aMatch(id = 2L, dateTime = 3000L)
            val middle = aMatch(id = 3L, dateTime = 2000L)
            coEvery { getTeamById(any()) } returns aTeam()
            every { getPlayersByTeam(any()) } returns flowOf(emptyList())
            every { getMatchesByTeam(any()) } returns flowOf(listOf(older, newer, middle))

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value as PresidentTeamDetailUiState.Ready
            assertEquals(listOf(2L, 3L, 1L), state.matches.map { it.id })
        }

    @Test
    fun `selectTab changes selectedTab state`() =
        runTest {
            coEvery { getTeamById(any()) } returns aTeam()
            every { getPlayersByTeam(any()) } returns flowOf(emptyList())
            every { getMatchesByTeam(any()) } returns flowOf(emptyList())
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.selectTab(PresidentTeamTab.PLAYERS)
            assertEquals(PresidentTeamTab.PLAYERS, viewModel.selectedTab.value)

            viewModel.selectTab(PresidentTeamTab.MATCHES)
            assertEquals(PresidentTeamTab.MATCHES, viewModel.selectedTab.value)

            viewModel.selectTab(PresidentTeamTab.STATS)
            assertEquals(PresidentTeamTab.STATS, viewModel.selectedTab.value)

            viewModel.selectTab(PresidentTeamTab.SUMMARY)
            assertEquals(PresidentTeamTab.SUMMARY, viewModel.selectedTab.value)
        }

    @Test
    fun `when no matches and no players all stats are zero`() =
        runTest {
            coEvery { getTeamById(any()) } returns aTeam()
            every { getPlayersByTeam(any()) } returns flowOf(emptyList())
            every { getMatchesByTeam(any()) } returns flowOf(emptyList())

            val viewModel = createViewModel()
            advanceUntilIdle()

            val stats = (viewModel.uiState.value as PresidentTeamDetailUiState.Ready).stats
            assertTrue(
                stats == PresidentTeamStats(
                    totalMatches = 0,
                    wins = 0,
                    draws = 0,
                    losses = 0,
                    goalsScored = 0,
                    goalsConceded = 0,
                    squadSize = 0,
                ),
            )
        }
}
