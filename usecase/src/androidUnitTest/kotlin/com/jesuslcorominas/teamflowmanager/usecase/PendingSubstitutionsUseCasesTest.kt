package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.usecase.repository.PendingSubstitutionsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * The five use cases over the pending store are thin by design: they exist to keep the match
 * ViewModel off `usecase.repository`. What is worth pinning down is therefore exactly that — each
 * one reaches the repository method it claims to, with the arguments untouched, and adds nothing.
 */
class PendingSubstitutionsUseCasesTest {
    private lateinit var repository: PendingSubstitutionsRepository

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `givenStoredPendings_whenObserve_thenEmitsThemUnchanged`() =
        runTest {
            // Given
            val stored = listOf(PAIR, OTHER_PAIR)
            every { repository.observe(MATCH_ID) } returns flowOf(stored)

            // When
            val result = ObservePendingSubstitutionsUseCaseImpl(repository)(MATCH_ID).toList()

            // Then — order is part of the store's contract, so compare the list, not its contents
            assertEquals(listOf(stored), result)
            verify(exactly = 1) { repository.observe(MATCH_ID) }
        }

    @Test
    fun `givenAConflictingPair_whenGetConflicts_thenReturnsWhatTheStoreWouldDiscard`() =
        runTest {
            // Given
            every { repository.conflictsFor(MATCH_ID, PAIR) } returns listOf(OTHER_PAIR)

            // When
            val result = GetPendingSubstitutionConflictsUseCaseImpl(repository)(MATCH_ID, PAIR)

            // Then
            assertEquals(listOf(OTHER_PAIR), result)
            verify(exactly = 1) { repository.conflictsFor(MATCH_ID, PAIR) }
        }

    @Test
    fun `givenAPair_whenAdd_thenSchedulesItWithoutRevalidating`() =
        runTest {
            // When
            AddPendingSubstitutionUseCaseImpl(repository)(MATCH_ID, PAIR)

            // Then — the store owns the invariants; the use case must not second-guess them
            verify(exactly = 1) { repository.add(MATCH_ID, PAIR) }
            verify(exactly = 0) { repository.conflictsFor(any(), any()) }
        }

    @Test
    fun `givenAPair_whenRemove_thenUnschedulesExactlyThatPair`() =
        runTest {
            // When
            RemovePendingSubstitutionUseCaseImpl(repository)(MATCH_ID, PAIR)

            // Then
            verify(exactly = 1) { repository.remove(MATCH_ID, PAIR) }
            verify(exactly = 0) { repository.clear(any()) }
        }

    @Test
    fun `givenAMatch_whenClear_thenEmptiesOnlyThatMatch`() =
        runTest {
            // When
            ClearPendingSubstitutionsUseCaseImpl(repository)(MATCH_ID)

            // Then
            verify(exactly = 1) { repository.clear(MATCH_ID) }
        }

    private companion object {
        const val MATCH_ID = "match-1"
        val PAIR = SubstitutionPair(playerOutId = "1", playerInId = "2")
        val OTHER_PAIR = SubstitutionPair(playerOutId = "3", playerInId = "4")
    }
}
