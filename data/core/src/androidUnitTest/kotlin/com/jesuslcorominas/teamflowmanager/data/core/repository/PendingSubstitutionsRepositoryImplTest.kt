package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PendingSubstitutionsDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
            // Given a real collector running while the writes happen
            val emissions = mutableListOf<List<SubstitutionPair>>()
            val collector =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.observe(MATCH_ID).toList(emissions)
                }

            // When
            repository.add(MATCH_ID, PAIR_1_2)
            repository.add(MATCH_ID, PAIR_3_4)

            // Then the collector saw the seed and both updates, not just the latest value
            assertEquals(
                listOf(emptyList(), listOf(PAIR_1_2), listOf(PAIR_1_2, PAIR_3_4)),
                emissions,
            )
            collector.cancel()
        }

    // --- degenerate pair ---

    @Test
    fun `givenSamePlayerOutAndIn_whenAdd_thenPairIsIgnored`() =
        runTest {
            // Given a pair where a player would substitute themselves
            val degenerate = SubstitutionPair(playerOutId = "p1", playerInId = "p1")

            // When
            repository.add(MATCH_ID, degenerate)

            // Then nothing is scheduled and nothing is written
            assertTrue(repository.observe(MATCH_ID).first().isEmpty())
            assertFalse(dataSource.storage.containsKey(MATCH_ID))
        }

    @Test
    fun `givenExistingPairs_whenAddDegeneratePair_thenExistingPairsAreKept`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)

            // When a degenerate pair reusing p1 is added, it must not discard the real one
            repository.add(MATCH_ID, SubstitutionPair(playerOutId = "p1", playerInId = "p1"))

            // Then
            assertEquals(listOf(PAIR_1_2), repository.observe(MATCH_ID).first())
        }

    // --- conflictsFor ---

    @Test
    fun `givenNoPendings_whenConflictsFor_thenReturnsEmpty`() =
        runTest {
            assertTrue(repository.conflictsFor(MATCH_ID, PAIR_1_2).isEmpty())
        }

    @Test
    fun `givenDisjointPairScheduled_whenConflictsFor_thenReturnsEmpty`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_3_4)

            // When / Then
            assertTrue(repository.conflictsFor(MATCH_ID, PAIR_1_2).isEmpty())
        }

    @Test
    fun `givenPairsSharingPlayers_whenConflictsFor_thenReturnsExactlyWhatAddWouldDiscard`() =
        runTest {
            // Given one pair that shares p1 with the candidate and one that does not
            repository.add(MATCH_ID, PAIR_1_2)
            repository.add(MATCH_ID, PAIR_3_4)

            // When
            val conflicts = repository.conflictsFor(MATCH_ID, PAIR_5_1)

            // Then the reported conflicts are the pairs add() actually drops
            assertEquals(listOf(PAIR_1_2), conflicts)
            repository.add(MATCH_ID, PAIR_5_1)
            assertEquals(listOf(PAIR_3_4, PAIR_5_1), repository.observe(MATCH_ID).first())
        }

    @Test
    fun `givenPairAlreadyScheduled_whenConflictsFor_thenReturnsEmptyBecauseAddIsANoOp`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)

            // When / Then an exact re-add discards nothing
            assertTrue(repository.conflictsFor(MATCH_ID, PAIR_1_2).isEmpty())
        }

    @Test
    fun `givenDegeneratePair_whenConflictsFor_thenReturnsEmptyBecauseAddIgnoresIt`() =
        runTest {
            // Given
            repository.add(MATCH_ID, PAIR_1_2)

            // When / Then
            assertTrue(
                repository
                    .conflictsFor(MATCH_ID, SubstitutionPair(playerOutId = "p1", playerInId = "p1"))
                    .isEmpty(),
            )
        }

    // --- clear over an unreadable payload ---

    @Test
    fun `givenCorruptedJsonInStorage_whenClear_thenCorruptEntryIsRemoved`() =
        runTest {
            // Given a payload that cannot be parsed, so the match already reads as empty
            dataSource.storage[MATCH_ID] = "{not json"
            val repositoryOverCorruptStorage = PendingSubstitutionsRepositoryImpl(dataSource)
            assertTrue(repositoryOverCorruptStorage.observe(MATCH_ID).first().isEmpty())

            // When
            repositoryOverCorruptStorage.clear(MATCH_ID)

            // Then clear does not skip the write: the corrupt entry is gone for good
            assertFalse(dataSource.storage.containsKey(MATCH_ID))
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
