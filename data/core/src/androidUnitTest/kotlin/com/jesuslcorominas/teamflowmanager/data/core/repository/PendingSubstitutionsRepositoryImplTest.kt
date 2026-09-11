package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PendingSubstitutionsDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val MATCH_ID = "match-1"
private const val OTHER_MATCH_ID = "match-2"

private val PAIR_1_2 = SubstitutionPair(playerOutId = "p1", playerInId = "p2")
private val PAIR_3_4 = SubstitutionPair(playerOutId = "p3", playerInId = "p4")
private val PAIR_5_6 = SubstitutionPair(playerOutId = "p5", playerInId = "p6")
private val PAIR_1_9 = SubstitutionPair(playerOutId = "p1", playerInId = "p9")
private val PAIR_9_2 = SubstitutionPair(playerOutId = "p9", playerInId = "p2")
private val PAIR_5_1 = SubstitutionPair(playerOutId = "p5", playerInId = "p1")
private val PAIR_2_7 = SubstitutionPair(playerOutId = "p2", playerInId = "p7")

class PendingSubstitutionsRepositoryImplTest {
    private lateinit var dataSource: FakePendingSubstitutionsDataSource
    private lateinit var repository: PendingSubstitutionsRepositoryImpl

    @Before
    fun setup() {
        dataSource = FakePendingSubstitutionsDataSource()
        repository = PendingSubstitutionsRepositoryImpl(dataSource)
    }

    @Test
    fun `givenNothingStored_whenObserve_thenEmitsEmptyList`() =
        runTest {
            // Given nothing stored for the match
            // When
            val result = repository.observe(MATCH_ID).first()

            // Then
            assertEquals(emptyList<SubstitutionPair>(), result)
        }

    @Test
    fun `givenEmptyStore_whenAddPair_thenObserveEmitsThatPair`() =
        runTest {
            // Given an empty store
            // When
            repository.add(MATCH_ID, PAIR_1_2)

            // Then
            assertEquals(listOf(PAIR_1_2), repository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenEmptyStore_whenAddPair_thenPersistsJsonInDataSource`() =
        runTest {
            // Given an empty store
            // When
            repository.add(MATCH_ID, PAIR_1_2)

            // Then
            assertEquals("""[{"out":"p1","in":"p2"}]""", dataSource.storage[MATCH_ID])
        }

    @Test
    fun `givenTwoDisjointPairs_whenAdd_thenBothKeptInInsertionOrder`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)

            // When
            repository.add(MATCH_ID, PAIR_3_4)

            // Then
            assertEquals(listOf(PAIR_1_2, PAIR_3_4), repository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenExistingPair_whenAddPairWithSameOutgoingPlayer_thenPreviousPairIsDropped`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)

            // When
            repository.add(MATCH_ID, PAIR_1_9)

            // Then
            assertEquals(listOf(PAIR_1_9), repository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenExistingPair_whenAddPairWithSameIncomingPlayer_thenPreviousPairIsDropped`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)

            // When
            repository.add(MATCH_ID, PAIR_9_2)

            // Then
            assertEquals(listOf(PAIR_9_2), repository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenExistingPair_whenAddPairReusingOutgoingPlayerAsIncoming_thenPreviousPairIsDropped`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)

            // When the outgoing player of the previous pair becomes the incoming one
            repository.add(MATCH_ID, PAIR_5_1)

            // Then
            assertEquals(listOf(PAIR_5_1), repository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenExistingPair_whenAddPairReusingIncomingPlayerAsOutgoing_thenPreviousPairIsDropped`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)

            // When the incoming player of the previous pair becomes the outgoing one
            repository.add(MATCH_ID, PAIR_2_7)

            // Then
            assertEquals(listOf(PAIR_2_7), repository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenPairAlreadyAdded_whenAddSamePairAgain_thenNotDuplicated`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)

            // When
            repository.add(MATCH_ID, PAIR_1_2)

            // Then
            assertEquals(listOf(PAIR_1_2), repository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenPairAddedFirst_whenAddSamePairAgainAfterOthers_thenKeepsOriginalPosition`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)
            repository.add(MATCH_ID, PAIR_3_4)

            // When
            repository.add(MATCH_ID, PAIR_1_2)

            // Then the re-added pair is not moved to the end
            assertEquals(listOf(PAIR_1_2, PAIR_3_4), repository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenSeveralPairs_whenRemoveOne_thenOnlyThatPairIsDeleted`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)
            repository.add(MATCH_ID, PAIR_3_4)
            repository.add(MATCH_ID, PAIR_5_6)

            // When
            repository.remove(MATCH_ID, PAIR_3_4)

            // Then
            assertEquals(listOf(PAIR_1_2, PAIR_5_6), repository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenPairNotPresent_whenRemove_thenListUnchanged`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)

            // When
            repository.remove(MATCH_ID, PAIR_5_6)

            // Then
            assertEquals(listOf(PAIR_1_2), repository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenLastPairRemoved_whenObserve_thenEmitsEmptyAndStorageEntryIsCleared`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)

            // When
            repository.remove(MATCH_ID, PAIR_1_2)

            // Then
            assertEquals(emptyList<SubstitutionPair>(), repository.observe(MATCH_ID).first())
            assertFalse(dataSource.storage.containsKey(MATCH_ID))
        }

    @Test
    fun `givenSeveralPairs_whenClear_thenListIsEmpty`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)
            repository.add(MATCH_ID, PAIR_3_4)

            // When
            repository.clear(MATCH_ID)

            // Then
            assertEquals(emptyList<SubstitutionPair>(), repository.observe(MATCH_ID).first())
            assertFalse(dataSource.storage.containsKey(MATCH_ID))
        }

    @Test
    fun `givenPairsInTwoMatches_whenClearOneMatch_thenOtherMatchIsUnaffected`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)
            repository.add(OTHER_MATCH_ID, PAIR_3_4)

            // When
            repository.clear(MATCH_ID)

            // Then
            assertEquals(emptyList<SubstitutionPair>(), repository.observe(MATCH_ID).first())
            assertEquals(listOf(PAIR_3_4), repository.observe(OTHER_MATCH_ID).first())
        }

    @Test
    fun `givenPairsInTwoMatches_whenAddToOne_thenOtherMatchIsUnaffected`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)
            repository.add(OTHER_MATCH_ID, PAIR_3_4)

            // When
            repository.add(MATCH_ID, PAIR_5_6)

            // Then
            assertEquals(listOf(PAIR_1_2, PAIR_5_6), repository.observe(MATCH_ID).first())
            assertEquals(listOf(PAIR_3_4), repository.observe(OTHER_MATCH_ID).first())
        }

    @Test
    fun `givenCorruptedJsonInStorage_whenObserve_thenEmitsEmptyListWithoutThrowing`() =
        runTest {
            // Given corrupted content seeded before the repository reads it
            dataSource.storage[MATCH_ID] = "{not json"
            repository = PendingSubstitutionsRepositoryImpl(dataSource)

            // When
            val result = repository.observe(MATCH_ID).first()

            // Then
            assertEquals(emptyList<SubstitutionPair>(), result)
        }

    @Test
    fun `givenJsonWithWrongShapeInStorage_whenObserve_thenEmitsEmptyList`() =
        runTest {
            // Given a JSON object where an array is expected
            dataSource.storage[MATCH_ID] = """{"unexpected":true}"""
            repository = PendingSubstitutionsRepositoryImpl(dataSource)

            // When
            val result = repository.observe(MATCH_ID).first()

            // Then
            assertEquals(emptyList<SubstitutionPair>(), result)
        }

    @Test
    fun `givenCorruptedJson_whenAddPair_thenStoresOnlyTheNewPair`() =
        runTest {
            // Given
            dataSource.storage[MATCH_ID] = "{not json"
            repository = PendingSubstitutionsRepositoryImpl(dataSource)

            // When
            repository.add(MATCH_ID, PAIR_1_2)

            // Then
            assertEquals(listOf(PAIR_1_2), repository.observe(MATCH_ID).first())
            assertEquals("""[{"out":"p1","in":"p2"}]""", dataSource.storage[MATCH_ID])
        }

    @Test
    fun `givenPairsPersisted_whenNewRepositoryInstanceReadsSameStorage_thenSamePairsAreRestored`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)
            repository.add(MATCH_ID, PAIR_3_4)

            // When the app restarts: a brand new repository reads the same storage
            val restartedRepository = PendingSubstitutionsRepositoryImpl(dataSource)

            // Then
            assertEquals(listOf(PAIR_1_2, PAIR_3_4), restartedRepository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenObserverCollecting_whenAddPair_thenEmitsUpdatedList`() =
        runTest {
            // Given an already obtained flow
            val flow = repository.observe(MATCH_ID)
            assertTrue(flow.first().isEmpty())

            // When
            repository.add(MATCH_ID, PAIR_1_2)

            // Then the same flow instance emits the updated value
            assertEquals(listOf(PAIR_1_2), flow.first())
        }
}

private class FakePendingSubstitutionsDataSource : PendingSubstitutionsDataSource {
    val storage = mutableMapOf<String, String>()

    override fun getRaw(matchId: String): String? = storage[matchId]

    override fun setRaw(
        matchId: String,
        raw: String?,
    ) {
        if (raw == null) storage.remove(matchId) else storage[matchId] = raw
    }
}
