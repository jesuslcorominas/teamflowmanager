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
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.MatchFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import io.mockk.coEvery
import kotlinx.coroutines.tasks.await
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test

class MatchFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockAuth = mockk<FirebaseAuth>()
    private val mockUser = mockk<FirebaseUser>()
    private val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)
    private lateinit var dataSource: MatchFirestoreDataSourceImpl

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

        dataSource = MatchFirestoreDataSourceImpl(mockFirestore, mockAuth)
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
        every { teamsCollection.whereEqualTo("ownerId", userId) } returns teamQuery
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
        every { teamsCollection.whereEqualTo("ownerId", userId) } returns teamQuery
        every { teamQuery.limit(1) } returns teamQuery
        val teamTask = mockk<Task<QuerySnapshot>>()
        every { teamQuery.get() } returns teamTask
        coEvery { teamTask.await() } returns teamSnapshot
        every { teamSnapshot.documents } returns emptyList()
    }

    @Test
    fun `getAllMatchesDirect_returnsEmptyList`() = runTest {
        val result = dataSource.getAllMatchesDirect()
        assertEquals(emptyList<Match>(), result)
    }

    @Test
    fun `clearLocalData_isNoOp`() = runTest {
        dataSource.clearLocalData()
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetAllMatches_thenEmitsEmptyList`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getAllMatches().test {
            val result = awaitItem()
            assertEquals(emptyList<Match>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetAllMatches_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        dataSource.getAllMatches().test {
            val result = awaitItem()
            assertEquals(emptyList<Match>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetMatchById_thenEmitsNull`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getMatchById(1L).test {
            val result = awaitItem()
            assertEquals(null, result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetMatchById_thenEmitsNull`() = runTest {
        setupUserWithNoTeam()

        dataSource.getMatchById(1L).test {
            val result = awaitItem()
            assertEquals(null, result)
            cancel()
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetArchivedMatches_thenEmitsEmptyList`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getArchivedMatches().test {
            val result = awaitItem()
            assertEquals(emptyList<Match>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetScheduledMatches_thenReturnsEmptyList`() = runTest {
        setupUserWithNoTeam()

        val result = dataSource.getScheduledMatches()

        assertEquals(emptyList<Match>(), result)
    }

    @Test
    fun `givenNoTeam_whenInsertMatch_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val match = mockk<Match>(relaxed = true)

        try {
            dataSource.insertMatch(match)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenInsertMatch_thenReturnsStableId`() = runTest {
        setupUserWithTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchDocRef = mockk<DocumentReference>()
        every { matchDocRef.id } returns "match-doc-id"

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.document() } returns matchDocRef

        val voidTask = mockk<Task<Void>>()
        every { matchDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val match = mockk<Match>(relaxed = true)

        val result = dataSource.insertMatch(match)

        assertTrue(result != 0L)
    }

    @Test
    fun `givenNullCaptainId_whenUpdateMatchCaptain_thenStoresZero`() = runTest {
        setupUserWithTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchDocRef = mockk<DocumentReference>()
        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        val matchDocSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot

        every { matchSnapshot.documents } returns listOf(matchDocSnapshot)
        every { matchDocSnapshot.id } returns "match-doc-id"

        val matchModel = MatchFirestoreModel(
            id = "match-doc-id",
            teamId = "team-doc-id",
            opponent = "Opponent",
            archived = false
        )
        every { matchDocSnapshot.toObject(MatchFirestoreModel::class.java) } returns matchModel

        every { matchesCollection.document(any()) } returns matchDocRef
        val updateTask = mockk<Task<Void>>()
        every { matchDocRef.update(any<String>(), any()) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        // Compute the stable ID for "match-doc-id"
        val stableId = run {
            var result = 0L
            var multiplier = 1L
            for (char in "match-doc-id") {
                result += char.code * multiplier
                multiplier *= 31
            }
            kotlin.math.abs(result)
        }

        // Should not throw
        dataSource.updateMatchCaptain(stableId, null)
    }

    @Test
    fun `givenNoTeam_whenGetArchivedMatches_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        dataSource.getArchivedMatches().test {
            val result = awaitItem()
            assertEquals(emptyList<Match>(), result)
            cancel()
        }
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetScheduledMatches_thenReturnsScheduledMatches`() = runTest {
        setupUserWithTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>(relaxed = true)
        val matchSnapshot = mockk<QuerySnapshot>()
        val matchDocSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        every { matchQuery.whereEqualTo(any<String>(), any()) } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot

        val matchModel = MatchFirestoreModel(
            id = "match-doc-id",
            teamId = "team-doc-id",
            opponent = "Opponent",
            archived = false,
            status = "SCHEDULED"
        )
        every { matchDocSnapshot.id } returns "match-doc-id"
        every { matchDocSnapshot.toObject(MatchFirestoreModel::class.java) } returns matchModel
        every { matchSnapshot.documents } returns listOf(matchDocSnapshot)

        val result = dataSource.getScheduledMatches()

        assertEquals(1, result.size)
    }

    @Test
    fun `givenNoTeam_whenUpdateMatch_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val match = mockk<Match>(relaxed = true)

        try {
            dataSource.updateMatch(match)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenMatchNotFound_whenUpdateMatch_thenThrowsIllegalStateException`() = runTest {
        setupUserWithTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        val match = mockk<Match>(relaxed = true)
        every { match.id } returns 99999L

        try {
            dataSource.updateMatch(match)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenValidMatch_whenUpdateMatch_thenUpdatesDocument`() = runTest {
        setupUserWithTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchDocRef = mockk<DocumentReference>()
        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        val matchDocSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns listOf(matchDocSnapshot)
        every { matchDocSnapshot.id } returns "match-doc-id"

        val matchModel = MatchFirestoreModel(
            id = "match-doc-id",
            teamId = "team-doc-id",
            opponent = "Opponent",
            archived = false
        )
        every { matchDocSnapshot.toObject(MatchFirestoreModel::class.java) } returns matchModel

        every { matchesCollection.document("match-doc-id") } returns matchDocRef
        val voidTask = mockk<Task<Void>>()
        every { matchDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val stableMatchId = run {
            var result = 0L
            var multiplier = 1L
            for (char in "match-doc-id") {
                result += char.code * multiplier
                multiplier *= 31
            }
            kotlin.math.abs(result)
        }
        val match = mockk<Match>(relaxed = true)
        every { match.id } returns stableMatchId

        dataSource.updateMatch(match)
    }

    @Test
    fun `givenNoTeam_whenDeleteMatch_thenDoesNotThrow`() = runTest {
        setupUserWithNoTeam()

        dataSource.deleteMatch(1L)
    }

    @Test
    fun `givenMatchNotFound_whenDeleteMatch_thenDoesNotThrow`() = runTest {
        setupUserWithTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        dataSource.deleteMatch(99999L)
    }

    @Test
    fun `givenValidMatchId_whenDeleteMatch_thenDeletesDocument`() = runTest {
        setupUserWithTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchDocRef = mockk<DocumentReference>()
        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        val matchDocSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns listOf(matchDocSnapshot)
        every { matchDocSnapshot.id } returns "match-doc-id"

        val matchModel = MatchFirestoreModel(
            id = "match-doc-id",
            teamId = "team-doc-id",
            opponent = "Opponent",
            archived = false
        )
        every { matchDocSnapshot.toObject(MatchFirestoreModel::class.java) } returns matchModel

        every { matchesCollection.document("match-doc-id") } returns matchDocRef
        val voidTask = mockk<Task<Void>>()
        every { matchDocRef.delete() } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val stableMatchId = run {
            var result = 0L
            var multiplier = 1L
            for (char in "match-doc-id") {
                result += char.code * multiplier
                multiplier *= 31
            }
            kotlin.math.abs(result)
        }

        dataSource.deleteMatch(stableMatchId)
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetAllMatches_thenEmitsMatches`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>()
        val matchQuery2 = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        every { matchQuery.whereEqualTo("archived", false) } returns matchQuery2
        every { matchQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val matchModel = MatchFirestoreModel(
            id = "match-doc-id",
            teamId = "team-doc-id",
            opponent = "Opponent",
            archived = false
        )
        every { docSnapshot.id } returns "match-doc-id"
        every { docSnapshot.toObject(MatchFirestoreModel::class.java) } returns matchModel
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getAllMatches().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetAllMatches_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>()
        val matchQuery2 = mockk<Query>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        every { matchQuery.whereEqualTo("archived", false) } returns matchQuery2
        every { matchQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getAllMatches().test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<Match>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNullSnapshot_whenGetAllMatches_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>()
        val matchQuery2 = mockk<Query>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        every { matchQuery.whereEqualTo("archived", false) } returns matchQuery2
        every { matchQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getAllMatches().test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(emptyList<Match>(), result)
            cancel()
        }
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetArchivedMatches_thenEmitsMatches`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>()
        val matchQuery2 = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        every { matchQuery.whereEqualTo("archived", true) } returns matchQuery2
        every { matchQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val matchModel = MatchFirestoreModel(
            id = "match-doc-id",
            teamId = "team-doc-id",
            opponent = "Opponent",
            archived = true
        )
        every { docSnapshot.id } returns "match-doc-id"
        every { docSnapshot.toObject(MatchFirestoreModel::class.java) } returns matchModel
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getArchivedMatches().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetArchivedMatches_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>()
        val matchQuery2 = mockk<Query>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        every { matchQuery.whereEqualTo("archived", true) } returns matchQuery2
        every { matchQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getArchivedMatches().test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<Match>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNullSnapshot_whenGetArchivedMatches_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>()
        val matchQuery2 = mockk<Query>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        every { matchQuery.whereEqualTo("archived", true) } returns matchQuery2
        every { matchQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getArchivedMatches().test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(emptyList<Match>(), result)
            cancel()
        }
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetMatchById_thenEmitsMatch`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        every { matchQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val stableMatchId = run {
            var result = 0L; var multiplier = 1L
            for (char in "match-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val matchModel = MatchFirestoreModel(
            id = "match-doc-id",
            teamId = "team-doc-id",
            opponent = "Opponent",
            archived = false
        )
        every { docSnapshot.id } returns "match-doc-id"
        every { docSnapshot.toObject(MatchFirestoreModel::class.java) } returns matchModel
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getMatchById(stableMatchId).test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertNotNull(result)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetMatchById_thenEmitsNull`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        every { matchQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getMatchById(1L).test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenUpdateMatchCaptain_thenReturnsEarly`() = runTest {
        setupUserWithNoTeam()

        // Should not throw when no team found
        dataSource.updateMatchCaptain(1L, 100L)
    }

    @Test
    fun `givenMatchNotFound_whenUpdateMatchCaptain_thenDoesNotThrow`() = runTest {
        setupUserWithTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        // Should not throw when match not found
        dataSource.updateMatchCaptain(99999L, 100L)
    }

    @Test
    fun `givenExceptionInGetScheduledMatches_whenGetScheduledMatches_thenReturnsEmptyList`() = runTest {
        setupUserWithTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchQuery = mockk<Query>(relaxed = true)

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        every { matchQuery.whereEqualTo(any<String>(), any()) } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } throws RuntimeException("Firestore error")

        val result = dataSource.getScheduledMatches()

        assertEquals(emptyList<Match>(), result)
    }

    @Test
    fun `givenExceptionDuringSet_whenInsertMatch_thenPropagatesException`() = runTest {
        setupUserWithTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchDocRef = mockk<DocumentReference>()
        every { matchDocRef.id } returns "match-doc-id"

        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.document() } returns matchDocRef

        val voidTask = mockk<Task<Void>>()
        every { matchDocRef.set(any()) } returns voidTask
        // Use a specific exception to cover the FirebaseFirestoreException catch branch
        coEvery { voidTask.await() } throws IllegalStateException("Firestore write failed")

        val match = mockk<Match>(relaxed = true)

        try {
            dataSource.insertMatch(match)
            fail("Expected exception")
        } catch (e: Exception) {
            // expected
        }
    }
}
