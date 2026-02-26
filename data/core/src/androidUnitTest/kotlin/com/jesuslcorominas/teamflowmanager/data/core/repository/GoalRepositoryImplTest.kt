package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GoalRepositoryImplTest {

    private lateinit var goalDataSource: GoalDataSource
    private lateinit var repository: GoalRepositoryImpl

    @Before
    fun setup() {
        goalDataSource = mockk(relaxed = true)
        repository = GoalRepositoryImpl(goalDataSource)
    }

    private fun createGoal(
        id: Long = 0L,
        matchId: Long = 1L,
        scorerId: Long? = 10L,
        isOpponentGoal: Boolean = false,
    ) = Goal(
        id = id,
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = 60_000L,
        matchElapsedTimeMillis = 60_000L,
        isOpponentGoal = isOpponentGoal,
    )

    // --- getMatchGoals ---

    @Test
    fun `givenMatchWithGoals_whenGetMatchGoals_thenDelegatesToDataSource`() = runTest {
        val matchId = 1L
        val goals = listOf(createGoal(id = 1L, matchId = matchId), createGoal(id = 2L, matchId = matchId))
        every { goalDataSource.getMatchGoals(matchId) } returns flowOf(goals)

        val result = repository.getMatchGoals(matchId).first()

        assertEquals(goals, result)
    }

    @Test
    fun `givenMatchWithNoGoals_whenGetMatchGoals_thenReturnsEmptyList`() = runTest {
        val matchId = 99L
        every { goalDataSource.getMatchGoals(matchId) } returns flowOf(emptyList())

        val result = repository.getMatchGoals(matchId).first()

        assertEquals(emptyList<Goal>(), result)
    }

    @Test
    fun `givenMatchWithOpponentGoal_whenGetMatchGoals_thenIncludesOpponentGoal`() = runTest {
        val matchId = 1L
        val goals = listOf(
            createGoal(id = 1L, isOpponentGoal = false),
            createGoal(id = 2L, isOpponentGoal = true, scorerId = null),
        )
        every { goalDataSource.getMatchGoals(matchId) } returns flowOf(goals)

        val result = repository.getMatchGoals(matchId).first()

        assertEquals(2, result.size)
        assertEquals(goals, result)
    }

    // --- getAllTeamGoals ---

    @Test
    fun `givenTeamGoals_whenGetAllTeamGoals_thenDelegatesToDataSource`() = runTest {
        val goals = listOf(
            createGoal(id = 1L, matchId = 1L),
            createGoal(id = 2L, matchId = 2L),
        )
        every { goalDataSource.getAllTeamGoals() } returns flowOf(goals)

        val result = repository.getAllTeamGoals().first()

        assertEquals(goals, result)
    }

    @Test
    fun `givenNoTeamGoals_whenGetAllTeamGoals_thenReturnsEmptyList`() = runTest {
        every { goalDataSource.getAllTeamGoals() } returns flowOf(emptyList())

        val result = repository.getAllTeamGoals().first()

        assertEquals(emptyList<Goal>(), result)
    }

    // --- insertGoal ---

    @Test
    fun `givenGoal_whenInsertGoal_thenReturnsInsertedId`() = runTest {
        val goal = createGoal()
        coEvery { goalDataSource.insertGoal(goal) } returns 5L

        val result = repository.insertGoal(goal)

        assertEquals(5L, result)
        coVerify { goalDataSource.insertGoal(goal) }
    }
}
