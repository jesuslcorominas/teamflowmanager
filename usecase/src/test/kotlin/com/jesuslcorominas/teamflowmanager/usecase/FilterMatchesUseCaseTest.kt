package com.jesuslcorominas.teamflowmanager.usecase

import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FilterMatchesUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var filterMatchesUseCase: FilterMatchesUseCase

    private val match1 = Match(
        id = 1L,
        opponent = "Real Madrid",
        location = "Santiago Bernabéu",
        date = 1700000000000L, // Nov 14, 2023
        archived = false,
    )
    
    private val match2 = Match(
        id = 2L,
        opponent = "Barcelona",
        location = "Camp Nou",
        date = 1705000000000L, // Jan 11, 2024
        archived = false,
    )
    
    private val match3 = Match(
        id = 3L,
        opponent = "Atletico Madrid",
        location = "Wanda Metropolitano",
        date = 1710000000000L, // Mar 9, 2024
        archived = true, // Archived match
    )
    
    private val match4 = Match(
        id = 4L,
        opponent = "Sevilla",
        location = "Ramon Sanchez Pizjuan",
        date = 1715000000000L, // May 6, 2024
        archived = false,
    )

    @Before
    fun setup() {
        matchRepository = mockk()
        filterMatchesUseCase = FilterMatchesUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should return all matches when filter text is empty`() = runTest {
        // Given
        val activeMatches = listOf(match1, match2, match4)
        val archivedMatches = listOf(match3)
        every { matchRepository.getAllMatches() } returns flowOf(activeMatches)
        every { matchRepository.getArchivedMatches() } returns flowOf(archivedMatches)

        // When & Then
        filterMatchesUseCase.invoke("").test {
            val result = awaitItem()
            assertEquals(4, result.size)
            assertEquals(listOf(match1, match2, match4, match3), result)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should filter matches by opponent name`() = runTest {
        // Given
        val activeMatches = listOf(match1, match2, match4)
        val archivedMatches = listOf(match3)
        every { matchRepository.getAllMatches() } returns flowOf(activeMatches)
        every { matchRepository.getArchivedMatches() } returns flowOf(archivedMatches)

        // When & Then
        filterMatchesUseCase.invoke("Madrid").test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals(listOf(match1, match3), result)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should filter matches by location`() = runTest {
        // Given
        val activeMatches = listOf(match1, match2, match4)
        val archivedMatches = listOf(match3)
        every { matchRepository.getAllMatches() } returns flowOf(activeMatches)
        every { matchRepository.getArchivedMatches() } returns flowOf(archivedMatches)

        // When & Then
        filterMatchesUseCase.invoke("Camp").test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(match2, result.first())
            awaitComplete()
        }
    }

    @Test
    fun `invoke should be case insensitive`() = runTest {
        // Given
        val activeMatches = listOf(match1, match2, match4)
        val archivedMatches = listOf(match3)
        every { matchRepository.getAllMatches() } returns flowOf(activeMatches)
        every { matchRepository.getArchivedMatches() } returns flowOf(archivedMatches)

        // When & Then
        filterMatchesUseCase.invoke("BARCELONA").test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(match2, result.first())
            awaitComplete()
        }
    }

    @Test
    fun `invoke should include archived matches in results`() = runTest {
        // Given
        val activeMatches = listOf(match1, match2, match4)
        val archivedMatches = listOf(match3)
        every { matchRepository.getAllMatches() } returns flowOf(activeMatches)
        every { matchRepository.getArchivedMatches() } returns flowOf(archivedMatches)

        // When & Then - filter for "Atletico" which is archived
        filterMatchesUseCase.invoke("Atletico").test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(match3, result.first())
            assertEquals(true, result.first().archived)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should filter by date range`() = runTest {
        // Given
        val activeMatches = listOf(match1, match2, match4)
        val archivedMatches = listOf(match3)
        every { matchRepository.getAllMatches() } returns flowOf(activeMatches)
        every { matchRepository.getArchivedMatches() } returns flowOf(archivedMatches)

        // When & Then - filter for dates in January-March 2024
        val startDate = 1704067200000L // Jan 1, 2024
        val endDate = 1711929599000L // Mar 31, 2024
        filterMatchesUseCase.invoke("", startDate, endDate).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals(listOf(match2, match3), result)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should filter by both text and date range`() = runTest {
        // Given
        val activeMatches = listOf(match1, match2, match4)
        val archivedMatches = listOf(match3)
        every { matchRepository.getAllMatches() } returns flowOf(activeMatches)
        every { matchRepository.getArchivedMatches() } returns flowOf(archivedMatches)

        // When & Then - filter for "Madrid" in January-March 2024
        val startDate = 1704067200000L // Jan 1, 2024
        val endDate = 1711929599000L // Mar 31, 2024
        filterMatchesUseCase.invoke("Madrid", startDate, endDate).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(match3, result.first())
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return empty list when no matches found`() = runTest {
        // Given
        val activeMatches = listOf(match1, match2, match4)
        val archivedMatches = listOf(match3)
        every { matchRepository.getAllMatches() } returns flowOf(activeMatches)
        every { matchRepository.getArchivedMatches() } returns flowOf(archivedMatches)

        // When & Then
        filterMatchesUseCase.invoke("NonExistentTeam").test {
            val result = awaitItem()
            assertEquals(0, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should handle matches without dates when filtering by date range`() = runTest {
        // Given
        val matchWithoutDate = Match(id = 5L, opponent = "Valencia", location = "Mestalla", date = null)
        val activeMatches = listOf(match1, matchWithoutDate)
        val archivedMatches = emptyList<Match>()
        every { matchRepository.getAllMatches() } returns flowOf(activeMatches)
        every { matchRepository.getArchivedMatches() } returns flowOf(archivedMatches)

        // When & Then - filter by date range
        val startDate = 1700000000000L
        val endDate = 1705000000000L
        filterMatchesUseCase.invoke("", startDate, endDate).test {
            val result = awaitItem()
            // Only match1 should be included (matchWithoutDate doesn't have date)
            assertEquals(1, result.size)
            assertEquals(match1, result.first())
            awaitComplete()
        }
    }

    @Test
    fun `invoke should trim whitespace from filter text`() = runTest {
        // Given
        val activeMatches = listOf(match1, match2, match4)
        val archivedMatches = listOf(match3)
        every { matchRepository.getAllMatches() } returns flowOf(activeMatches)
        every { matchRepository.getArchivedMatches() } returns flowOf(archivedMatches)

        // When & Then
        filterMatchesUseCase.invoke("  Barcelona  ").test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(match2, result.first())
            awaitComplete()
        }
    }
}
