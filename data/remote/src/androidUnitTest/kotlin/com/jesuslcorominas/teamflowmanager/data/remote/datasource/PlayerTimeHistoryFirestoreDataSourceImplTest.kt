package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestoreException
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
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerTimeHistoryFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import io.mockk.coEvery
import kotlinx.coroutines.tasks.await
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlayerTimeHistoryFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockAuth = mockk<FirebaseAuth>()
    private val mockUser = mockk<FirebaseUser>()
    private val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)
    private lateinit var dataSource: PlayerTimeHistoryFirestoreDataSourceImpl

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

        dataSource = PlayerTimeHistoryFirestoreDataSourceImpl(mockFirestore, mockAuth)
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
    fun `getAllPlayerTimeHistoryDirect_returnsEmptyList`() = runTest {
        val result = dataSource.getAllPlayerTimeHistoryDirect()
        assertEquals(emptyList<PlayerTimeHistory>(), result)
    }

    @Test
    fun `clearLocalData_isNoOp`() = runTest {
        dataSource.clearLocalData()
    }

    @Test
    fun `givenNoTeam_whenInsertPlayerTimeHistory_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val history = mockk<PlayerTimeHistory>(relaxed = true)

        try {
            dataSource.insertPlayerTimeHistory(history)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getPlayerTimeHistory(1L).test {
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        dataSource.getPlayerTimeHistory(1L).test {
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetMatchPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getMatchPlayerTimeHistory(1L).test {
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetAllPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getAllPlayerTimeHistory().test {
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetMatchPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        dataSource.getMatchPlayerTimeHistory(1L).test {
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetAllPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        dataSource.getAllPlayerTimeHistory().test {
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetPlayerTimeHistory_thenEmitsList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val historyCollection = mockk<CollectionReference>()
        val historyQuery = mockk<Query>()
        val historyQuery2 = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { historyCollection.whereEqualTo("teamId", "team-doc-id") } returns historyQuery
        every { historyQuery.whereEqualTo("playerId", 1L) } returns historyQuery2
        every { historyQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val model = PlayerTimeHistoryFirestoreModel(
            id = "history-doc-id",
            teamId = "team-doc-id",
            playerId = 1L,
            matchId = 1L,
            elapsedTimeMillis = 45000L,
            savedAtMillis = System.currentTimeMillis()
        )
        every { docSnapshot.toObject(PlayerTimeHistoryFirestoreModel::class.java) } returns model
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getPlayerTimeHistory(1L).test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val historyCollection = mockk<CollectionReference>()
        val historyQuery = mockk<Query>()
        val historyQuery2 = mockk<Query>()

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { historyCollection.whereEqualTo("teamId", "team-doc-id") } returns historyQuery
        every { historyQuery.whereEqualTo("playerId", 1L) } returns historyQuery2
        every { historyQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getPlayerTimeHistory(1L).test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetMatchPlayerTimeHistory_thenEmitsList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val historyCollection = mockk<CollectionReference>()
        val historyQuery = mockk<Query>()
        val historyQuery2 = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { historyCollection.whereEqualTo("teamId", "team-doc-id") } returns historyQuery
        every { historyQuery.whereEqualTo("matchId", 1L) } returns historyQuery2
        every { historyQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val model = PlayerTimeHistoryFirestoreModel(
            id = "history-doc-id",
            teamId = "team-doc-id",
            playerId = 1L,
            matchId = 1L,
            elapsedTimeMillis = 45000L,
            savedAtMillis = System.currentTimeMillis()
        )
        every { docSnapshot.toObject(PlayerTimeHistoryFirestoreModel::class.java) } returns model
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getMatchPlayerTimeHistory(1L).test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetMatchPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val historyCollection = mockk<CollectionReference>()
        val historyQuery = mockk<Query>()
        val historyQuery2 = mockk<Query>()

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { historyCollection.whereEqualTo("teamId", "team-doc-id") } returns historyQuery
        every { historyQuery.whereEqualTo("matchId", 1L) } returns historyQuery2
        every { historyQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getMatchPlayerTimeHistory(1L).test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetAllPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val historyCollection = mockk<CollectionReference>()
        val historyQuery = mockk<Query>()

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { historyCollection.whereEqualTo("teamId", "team-doc-id") } returns historyQuery
        every { historyQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getAllPlayerTimeHistory().test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }

    @Test
    fun `givenRuntimeException_whenInsertPlayerTimeHistory_thenPropagatesException`() = runTest {
        setupUserWithTeam()

        val historyCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val historyDocRef = mockk<DocumentReference>()
        every { historyDocRef.id } returns "history-doc-id"

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { historyCollection.document() } returns historyDocRef

        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        val voidTask = mockk<Task<Void>>()
        every { historyDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } throws RuntimeException("Firestore error")

        val history = mockk<PlayerTimeHistory>(relaxed = true)
        every { history.matchId } returns 1L

        try {
            dataSource.insertPlayerTimeHistory(history)
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            // expected
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenInsertPlayerTimeHistory_thenThrowsIllegalStateException`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns null

        val history = mockk<PlayerTimeHistory>(relaxed = true)

        try {
            dataSource.insertPlayerTimeHistory(history)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenValidHistory_whenInsertPlayerTimeHistory_thenReturnsStableId`() = runTest {
        setupUserWithTeam()

        val historyCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val historyDocRef = mockk<DocumentReference>()
        every { historyDocRef.id } returns "history-doc-id"

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { historyCollection.document() } returns historyDocRef

        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        val voidTask = mockk<Task<Void>>()
        every { historyDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val history = mockk<PlayerTimeHistory>(relaxed = true)
        every { history.matchId } returns 1L

        val result = dataSource.insertPlayerTimeHistory(history)

        assertTrue(result != 0L)
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetAllPlayerTimeHistory_thenEmitsList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val historyCollection = mockk<CollectionReference>()
        val historyQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { historyCollection.whereEqualTo("teamId", "team-doc-id") } returns historyQuery
        every { historyQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val model = PlayerTimeHistoryFirestoreModel(
            id = "history-doc-id",
            teamId = "team-doc-id",
            playerId = 1L,
            matchId = 1L,
            elapsedTimeMillis = 45000L,
            savedAtMillis = System.currentTimeMillis()
        )
        every { docSnapshot.toObject(PlayerTimeHistoryFirestoreModel::class.java) } returns model
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getAllPlayerTimeHistory().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenMatchFoundInFirestore_whenInsertPlayerTimeHistory_thenUsesMatchDocId`() = runTest {
        setupUserWithTeam()

        val historyCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val historyDocRef = mockk<DocumentReference>()
        every { historyDocRef.id } returns "history-doc-id"

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { historyCollection.document() } returns historyDocRef

        // Compute stable ID for "match-doc-id"
        val stableMatchId = run {
            var result = 0L; var multiplier = 1L
            for (char in "match-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        val matchDoc = mockk<DocumentSnapshot>()
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchDoc.id } returns "match-doc-id"
        every { matchSnapshot.documents } returns listOf(matchDoc)

        val voidTask = mockk<Task<Void>>()
        every { historyDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val history = mockk<PlayerTimeHistory>(relaxed = true)
        every { history.matchId } returns stableMatchId

        val result = dataSource.insertPlayerTimeHistory(history)

        assertTrue(result != 0L)
    }

    @Test
    fun `givenExceptionDuringSet_whenInsertPlayerTimeHistory_thenPropagatesException`() = runTest {
        setupUserWithTeam()

        val historyCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val historyDocRef = mockk<DocumentReference>()
        every { historyDocRef.id } returns "history-doc-id"

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { historyCollection.document() } returns historyDocRef

        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        val voidTask = mockk<Task<Void>>()
        every { historyDocRef.set(any()) } returns voidTask
        // Use a specific exception to cover the FirebaseFirestoreException catch branch
        coEvery { voidTask.await() } throws IllegalStateException("Firestore write failed")

        val history = mockk<PlayerTimeHistory>(relaxed = true)
        every { history.matchId } returns 1L

        try {
            dataSource.insertPlayerTimeHistory(history)
            fail("Expected exception")
        } catch (e: Exception) {
            // expected
        }
    }

    @Test
    fun `givenNullSnapshot_whenGetPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val historyCollection = mockk<CollectionReference>()
        val historyQuery = mockk<Query>()
        val historyQuery2 = mockk<Query>()

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { historyCollection.whereEqualTo("teamId", "team-doc-id") } returns historyQuery
        every { historyQuery.whereEqualTo("playerId", 1L) } returns historyQuery2
        every { historyQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getPlayerTimeHistory(1L).test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNullSnapshot_whenGetMatchPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val historyCollection = mockk<CollectionReference>()
        val historyQuery = mockk<Query>()
        val historyQuery2 = mockk<Query>()

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { historyCollection.whereEqualTo("teamId", "team-doc-id") } returns historyQuery
        every { historyQuery.whereEqualTo("matchId", 1L) } returns historyQuery2
        every { historyQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getMatchPlayerTimeHistory(1L).test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNullSnapshot_whenGetAllPlayerTimeHistory_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val historyCollection = mockk<CollectionReference>()
        val historyQuery = mockk<Query>()

        every { mockFirestore.collection("playerTimeHistory") } returns historyCollection
        every { historyCollection.whereEqualTo("teamId", "team-doc-id") } returns historyQuery
        every { historyQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getAllPlayerTimeHistory().test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(emptyList<PlayerTimeHistory>(), result)
            cancel()
        }
    }
}
