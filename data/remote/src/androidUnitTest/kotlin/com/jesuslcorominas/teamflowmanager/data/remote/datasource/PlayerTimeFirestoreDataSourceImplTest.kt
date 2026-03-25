package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.WriteBatch
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerTimeFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import io.mockk.coEvery
import kotlinx.coroutines.tasks.await
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlayerTimeFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockAuth = mockk<FirebaseAuth>()
    private val mockUser = mockk<FirebaseUser>()
    private val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)
    private lateinit var dataSource: PlayerTimeFirestoreDataSourceImpl

    @After
    fun tearDown() {
        unmockkAll()
    }

        @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0

        dataSource = PlayerTimeFirestoreDataSourceImpl(mockFirestore, mockAuth)
    }

    private fun setupUserWithTeam(userId: String = "user-123", teamDocId: String = "team-doc-id") {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns userId
        val teamsCollection = mockk<CollectionReference>()
        val teamQuery = mockk<Query>()
        val teamSnapshot = mockk<QuerySnapshot>()
        val teamDoc = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", userId) } returns teamQuery
        every { teamQuery.limit(1) } returns teamQuery
        val teamTask = mockk<Task<QuerySnapshot>>()
        every { teamQuery.get() } returns teamTask
        coEvery { teamTask.await() } returns teamSnapshot
        every { teamSnapshot.documents } returns listOf(teamDoc)
        every { teamDoc.id } returns teamDocId
    }

    private fun setupUserWithNoTeam(userId: String = "user-123") {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns userId
        val teamsCollection = mockk<CollectionReference>()
        val teamQuery = mockk<Query>()
        val teamSnapshot = mockk<QuerySnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", userId) } returns teamQuery
        every { teamQuery.limit(1) } returns teamQuery
        val teamTask = mockk<Task<QuerySnapshot>>()
        every { teamQuery.get() } returns teamTask
        coEvery { teamTask.await() } returns teamSnapshot
        every { teamSnapshot.documents } returns emptyList()
    }

    @Test
    fun `getAllPlayerTimesDirect_returnsEmptyList`() = runTest {
        val result = dataSource.getAllPlayerTimesDirect()
        assertEquals(emptyList<PlayerTime>(), result)
    }

    @Test
    fun `clearLocalData_isNoOp`() = runTest {
        dataSource.clearLocalData()
    }

    @Test
    fun `givenEmptyPlayerTimes_whenBatchUpsertPlayerTimes_thenDoesNothing`() = runTest {
        // Should not throw and should not interact with Firestore
        dataSource.batchUpsertPlayerTimes(emptyList())
    }

    @Test
    fun `givenNoTeam_whenUpsertPlayerTime_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val playerTime = PlayerTime(
            playerId = 1L,
            elapsedTimeMillis = 0L,
            isRunning = false,
            status = PlayerTimeStatus.ON_BENCH
        )

        try {
            dataSource.upsertPlayerTime(playerTime)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoTeam_whenBatchUpsertPlayerTimes_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val playerTimes = listOf(
            PlayerTime(
                playerId = 1L,
                elapsedTimeMillis = 0L,
                isRunning = false,
                status = PlayerTimeStatus.ON_BENCH
            )
        )

        try {
            dataSource.batchUpsertPlayerTimes(playerTimes)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoTeam_whenDeleteAllPlayerTimes_thenReturnsEarly`() = runTest {
        setupUserWithNoTeam()

        // Should not throw
        dataSource.deleteAllPlayerTimes()
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetPlayerTime_thenEmitsNull`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getPlayerTime(1L).test {
            val result = awaitItem()
            assertEquals(null, result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetPlayerTime_thenEmitsNull`() = runTest {
        setupUserWithNoTeam()

        dataSource.getPlayerTime(1L).test {
            val result = awaitItem()
            assertEquals(null, result)
            cancel()
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetPlayerTimesByMatch_thenEmitsEmptyList`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getPlayerTimesByMatch(MATCH_ID).test {
            val result = awaitItem()
            assertEquals(emptyList<PlayerTime>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetPlayerTimesByMatch_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        dataSource.getPlayerTimesByMatch(MATCH_ID).test {
            val result = awaitItem()
            assertEquals(emptyList<PlayerTime>(), result)
            cancel()
        }
    }

    @Test
    fun `givenValidPlayerTime_whenUpsertPlayerTime_thenUsesPlayerIdAsDocumentId`() = runTest {
        setupUserWithTeam()

        val playerTimesCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.document("player_1") } returns docRef

        val voidTask = mockk<Task<Void>>()
        every { docRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val playerTime = PlayerTime(
            playerId = 1L,
            elapsedTimeMillis = 5000L,
            isRunning = true,
            status = PlayerTimeStatus.PLAYING
        )

        // Should not throw
        dataSource.upsertPlayerTime(playerTime)
    }

    @Test
    fun `givenPlayerTimes_whenBatchUpsertPlayerTimes_thenCommitsBatch`() = runTest {
        setupUserWithTeam()

        val playerTimesCollection = mockk<CollectionReference>()
        val mockBatch = mockk<WriteBatch>(relaxed = true)
        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { mockFirestore.batch() } returns mockBatch

        val batchCommitTask = mockk<Task<Void>>()
        every { mockBatch.commit() } returns batchCommitTask
        coEvery { batchCommitTask.await() } returns mockk()

        val docRef1 = mockk<DocumentReference>()
        val docRef2 = mockk<DocumentReference>()
        every { playerTimesCollection.document("player_1") } returns docRef1
        every { playerTimesCollection.document("player_2") } returns docRef2

        val playerTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 0L, isRunning = false, status = PlayerTimeStatus.ON_BENCH),
            PlayerTime(playerId = 2L, elapsedTimeMillis = 0L, isRunning = false, status = PlayerTimeStatus.ON_BENCH)
        )

        // Should not throw
        dataSource.batchUpsertPlayerTimes(playerTimes)
    }

    @Test
    fun `givenNoTeam_whenGetPlayerTime_thenEmitsNullViaTeamQuery`() = runTest {
        setupUserWithNoTeam()

        dataSource.getPlayerTime(1L).test {
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenRuntimeException_whenUpsertPlayerTime_thenPropagatesException`() = runTest {
        setupUserWithTeam()

        val playerTimesCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.document("player_1") } returns docRef

        val voidTask = mockk<Task<Void>>()
        every { docRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } throws RuntimeException("Firestore error")

        val playerTime = PlayerTime(
            playerId = 1L,
            elapsedTimeMillis = 5000L,
            isRunning = true,
            status = PlayerTimeStatus.PLAYING
        )

        try {
            dataSource.upsertPlayerTime(playerTime)
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            // expected
        }
    }

    @Test
    fun `givenTeamWithPlayerTimes_whenDeleteAllPlayerTimes_thenDeletesEachDocument`() = runTest {
        setupUserWithTeam()

        val playerTimesCollection = mockk<CollectionReference>()
        val playerTimesQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()
        val docRef = mockk<DocumentReference>()

        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.whereEqualTo("teamId", "team-doc-id") } returns playerTimesQuery
        val queryTask = mockk<Task<QuerySnapshot>>()
        every { playerTimesQuery.get() } returns queryTask
        coEvery { queryTask.await() } returns querySnapshot
        every { querySnapshot.documents } returns listOf(docSnapshot)

        every { docSnapshot.id } returns "player_1"
        every { docSnapshot.reference } returns docRef
        val deleteTask = mockk<Task<Void>>()
        every { docRef.delete() } returns deleteTask
        coEvery { deleteTask.await() } returns mockk()

        dataSource.deleteAllPlayerTimes()
    }

    @Test
    fun `givenTeamWithNoPlayerTimes_whenDeleteAllPlayerTimes_thenDoesNotThrow`() = runTest {
        setupUserWithTeam()

        val playerTimesCollection = mockk<CollectionReference>()
        val playerTimesQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()

        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.whereEqualTo("teamId", "team-doc-id") } returns playerTimesQuery
        val queryTask = mockk<Task<QuerySnapshot>>()
        every { playerTimesQuery.get() } returns queryTask
        coEvery { queryTask.await() } returns querySnapshot
        every { querySnapshot.documents } returns emptyList()

        dataSource.deleteAllPlayerTimes()
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetPlayerTime_thenEmitsPlayerTime`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val playerTimesCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.document("player_1") } returns docRef
        every { docRef.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val model = PlayerTimeFirestoreModel(
            id = "player_1",
            teamId = "team-doc-id",
            playerId = 1L,
            elapsedTimeMillis = 5000L,
            isRunning = false,
            status = PlayerTimeStatus.ON_BENCH.name
        )
        every { docSnapshot.exists() } returns true
        every { docSnapshot.toObject(PlayerTimeFirestoreModel::class.java) } returns model

        dataSource.getPlayerTime(1L).test {
            listenerSlot.captured.onEvent(docSnapshot, null)
            val result = awaitItem()
            assertEquals(1L, result?.playerId)
            cancel()
        }
    }

    @Test
    fun `givenNullSnapshot_whenGetPlayerTime_thenEmitsNull`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val playerTimesCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.document("player_1") } returns docRef
        every { docRef.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        every { docSnapshot.exists() } returns false

        dataSource.getPlayerTime(1L).test {
            listenerSlot.captured.onEvent(docSnapshot, null)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenError_whenGetPlayerTime_thenEmitsNull`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val playerTimesCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()

        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.document("player_1") } returns docRef
        every { docRef.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getPlayerTime(1L).test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenError_whenGetPlayerTimesByMatch_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playerTimesCollection = mockk<CollectionReference>()
        val playerTimesQuery = mockk<Query>()
        val playerTimesQueryWithMatch = mockk<Query>()

        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.whereEqualTo("teamId", "team-doc-id") } returns playerTimesQuery
        every { playerTimesQuery.whereEqualTo("matchId", MATCH_ID) } returns playerTimesQueryWithMatch
        every { playerTimesQueryWithMatch.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getPlayerTimesByMatch(MATCH_ID).test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<PlayerTime>(), result)
            cancel()
        }
    }

    @Test
    fun `givenBatchUpsertThrowsException_whenBatchUpsertPlayerTimes_thenPropagatesException`() = runTest {
        setupUserWithTeam()

        val playerTimesCollection = mockk<CollectionReference>()
        val mockBatch = mockk<WriteBatch>(relaxed = true)
        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { mockFirestore.batch() } returns mockBatch

        val batchCommitTask = mockk<Task<Void>>()
        every { mockBatch.commit() } returns batchCommitTask
        coEvery { batchCommitTask.await() } throws RuntimeException("Batch failed")

        val docRef1 = mockk<DocumentReference>()
        every { playerTimesCollection.document("player_1") } returns docRef1

        val playerTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 0L, isRunning = false, status = PlayerTimeStatus.ON_BENCH)
        )

        try {
            dataSource.batchUpsertPlayerTimes(playerTimes)
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            // expected
        }
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetPlayerTimesByMatch_thenEmitsList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playerTimesCollection = mockk<CollectionReference>()
        val playerTimesQuery = mockk<Query>()
        val playerTimesQueryWithMatch = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.whereEqualTo("teamId", "team-doc-id") } returns playerTimesQuery
        every { playerTimesQuery.whereEqualTo("matchId", MATCH_ID) } returns playerTimesQueryWithMatch
        every { playerTimesQueryWithMatch.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val model = PlayerTimeFirestoreModel(
            id = "1",
            teamId = "team-doc-id",
            playerId = 1L,
            elapsedTimeMillis = 5000L,
            isRunning = false,
            status = PlayerTimeStatus.ON_BENCH.name
        )
        every { docSnapshot.id } returns "player_1"
        every { docSnapshot.toObject(PlayerTimeFirestoreModel::class.java) } returns model
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getPlayerTimesByMatch(MATCH_ID).test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(1L, result[0].playerId)
            cancel()
        }
    }

    @Test
    fun `givenNullSnapshot_whenGetPlayerTimesByMatch_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playerTimesCollection = mockk<CollectionReference>()
        val playerTimesQuery = mockk<Query>()
        val playerTimesQueryWithMatch = mockk<Query>()

        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.whereEqualTo("teamId", "team-doc-id") } returns playerTimesQuery
        every { playerTimesQuery.whereEqualTo("matchId", MATCH_ID) } returns playerTimesQueryWithMatch
        every { playerTimesQueryWithMatch.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getPlayerTimesByMatch(MATCH_ID).test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(emptyList<PlayerTime>(), result)
            cancel()
        }
    }

    @Test
    fun `givenTeamIdMismatch_whenGetPlayerTime_thenEmitsNull`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val playerTimesCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.document("player_1") } returns docRef
        every { docRef.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // Model has a different teamId than "team-doc-id"
        val model = PlayerTimeFirestoreModel(
            id = "player_1",
            teamId = "different-team-id",
            playerId = 1L,
            elapsedTimeMillis = 5000L,
            isRunning = false,
            status = PlayerTimeStatus.ON_BENCH.name
        )
        every { docSnapshot.exists() } returns true
        every { docSnapshot.toObject(PlayerTimeFirestoreModel::class.java) } returns model

        dataSource.getPlayerTime(1L).test {
            listenerSlot.captured.onEvent(docSnapshot, null)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenNullModel_whenGetPlayerTime_thenEmitsNull`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val playerTimesCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.document("player_1") } returns docRef
        every { docRef.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        every { docSnapshot.exists() } returns true
        every { docSnapshot.toObject(PlayerTimeFirestoreModel::class.java) } returns null

        dataSource.getPlayerTime(1L).test {
            listenerSlot.captured.onEvent(docSnapshot, null)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenDeleteAllPlayerTimesThrowsException_whenDeleteAllPlayerTimes_thenPropagatesException`() = runTest {
        setupUserWithTeam()

        val playerTimesCollection = mockk<CollectionReference>()
        val playerTimesQuery = mockk<Query>()

        every { mockFirestore.collection("playerTimes") } returns playerTimesCollection
        every { playerTimesCollection.whereEqualTo("teamId", "team-doc-id") } returns playerTimesQuery
        val queryTask = mockk<Task<QuerySnapshot>>()
        every { playerTimesQuery.get() } returns queryTask
        coEvery { queryTask.await() } throws RuntimeException("Firestore error")

        try {
            dataSource.deleteAllPlayerTimes()
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            // expected
        }
    }

    companion object {
        private const val MATCH_ID = 1L
    }
}
