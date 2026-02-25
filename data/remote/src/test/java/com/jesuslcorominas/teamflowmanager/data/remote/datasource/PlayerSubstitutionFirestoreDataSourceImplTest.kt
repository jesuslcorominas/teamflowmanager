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
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerSubstitutionFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
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

class PlayerSubstitutionFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockAuth = mockk<FirebaseAuth>()
    private val mockUser = mockk<FirebaseUser>()
    private val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)
    private lateinit var dataSource: PlayerSubstitutionFirestoreDataSourceImpl

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

        dataSource = PlayerSubstitutionFirestoreDataSourceImpl(mockFirestore, mockAuth)
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
    fun `getAllPlayerSubstitutionsDirect_returnsEmptyList`() = runTest {
        val result = dataSource.getAllPlayerSubstitutionsDirect()
        assertEquals(emptyList<PlayerSubstitution>(), result)
    }

    @Test
    fun `clearLocalData_isNoOp`() = runTest {
        dataSource.clearLocalData()
    }

    @Test
    fun `givenNoTeam_whenInsertSubstitution_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val substitution = mockk<PlayerSubstitution>(relaxed = true)

        try {
            dataSource.insertSubstitution(substitution)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetMatchSubstitutions_thenEmitsEmptyList`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getMatchSubstitutions(1L).test {
            val result = awaitItem()
            assertEquals(emptyList<PlayerSubstitution>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetMatchSubstitutions_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        dataSource.getMatchSubstitutions(1L).test {
            val result = awaitItem()
            assertEquals(emptyList<PlayerSubstitution>(), result)
            cancel()
        }
    }

    @Test
    fun `givenValidSubstitution_whenInsertSubstitution_thenReturnsStableId`() = runTest {
        setupUserWithTeam()

        val substitutionsCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val subDocRef = mockk<DocumentReference>()
        every { subDocRef.id } returns "sub-doc-id"

        every { mockFirestore.collection("substitutions") } returns substitutionsCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { substitutionsCollection.document() } returns subDocRef

        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        val voidTask = mockk<Task<Void>>()
        every { subDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val substitution = mockk<PlayerSubstitution>(relaxed = true)
        every { substitution.matchId } returns 1L

        val result = dataSource.insertSubstitution(substitution)

        assertTrue(result != 0L)
    }

    @Test
    fun `givenNoAuthenticatedUser_whenInsertSubstitution_thenThrowsIllegalStateException`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns null

        val substitution = mockk<PlayerSubstitution>(relaxed = true)

        try {
            dataSource.insertSubstitution(substitution)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenRuntimeException_whenInsertSubstitution_thenPropagatesException`() = runTest {
        setupUserWithTeam()

        val substitutionsCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val subDocRef = mockk<DocumentReference>()
        every { subDocRef.id } returns "sub-doc-id"

        every { mockFirestore.collection("substitutions") } returns substitutionsCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { substitutionsCollection.document() } returns subDocRef

        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        val voidTask = mockk<Task<Void>>()
        every { subDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } throws RuntimeException("Firestore error")

        val substitution = mockk<PlayerSubstitution>(relaxed = true)
        every { substitution.matchId } returns 1L

        try {
            dataSource.insertSubstitution(substitution)
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            // expected
        }
    }

    @Test
    fun `givenFirestoreError_whenGetMatchSubstitutions_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val subsCollection = mockk<CollectionReference>()
        val subsQuery = mockk<Query>()
        val subQuery2 = mockk<Query>()

        every { mockFirestore.collection("substitutions") } returns subsCollection
        every { subsCollection.whereEqualTo("teamId", "team-doc-id") } returns subsQuery
        every { subsQuery.whereEqualTo("matchId", 1L) } returns subQuery2
        every { subQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getMatchSubstitutions(1L).test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<PlayerSubstitution>(), result)
            cancel()
        }
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetMatchSubstitutions_thenEmitsList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val subsCollection = mockk<CollectionReference>()
        val subsQuery = mockk<Query>()
        val subQuery2 = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("substitutions") } returns subsCollection
        every { subsCollection.whereEqualTo("teamId", "team-doc-id") } returns subsQuery
        every { subsQuery.whereEqualTo("matchId", 1L) } returns subQuery2
        every { subQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val model = PlayerSubstitutionFirestoreModel(
            id = "sub-doc-id",
            teamId = "team-doc-id",
            matchId = 1L,
            playerInId = 10L,
            playerOutId = 20L,
            substitutionTimeMillis = 45000L
        )
        every { docSnapshot.toObject(PlayerSubstitutionFirestoreModel::class.java) } returns model
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getMatchSubstitutions(1L).test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenMatchFoundInFirestore_whenInsertSubstitution_thenUsesMatchDocId`() = runTest {
        setupUserWithTeam()

        val substitutionsCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val subDocRef = mockk<DocumentReference>()
        every { subDocRef.id } returns "sub-doc-id"

        every { mockFirestore.collection("substitutions") } returns substitutionsCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { substitutionsCollection.document() } returns subDocRef

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
        every { subDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val substitution = mockk<PlayerSubstitution>(relaxed = true)
        every { substitution.matchId } returns stableMatchId

        val result = dataSource.insertSubstitution(substitution)

        assertTrue(result != 0L)
    }

    @Test
    fun `givenExceptionDuringSet_whenInsertSubstitution_thenPropagatesException`() = runTest {
        setupUserWithTeam()

        val substitutionsCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val subDocRef = mockk<DocumentReference>()
        every { subDocRef.id } returns "sub-doc-id"

        every { mockFirestore.collection("substitutions") } returns substitutionsCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { substitutionsCollection.document() } returns subDocRef

        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        val voidTask = mockk<Task<Void>>()
        every { subDocRef.set(any()) } returns voidTask
        // Use a specific exception to cover the FirebaseFirestoreException catch branch
        coEvery { voidTask.await() } throws IllegalStateException("Firestore write failed")

        val substitution = mockk<PlayerSubstitution>(relaxed = true)
        every { substitution.matchId } returns 1L

        try {
            dataSource.insertSubstitution(substitution)
            fail("Expected exception")
        } catch (e: Exception) {
            // expected
        }
    }

    @Test
    fun `givenNullSnapshot_whenGetMatchSubstitutions_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val subsCollection = mockk<CollectionReference>()
        val subsQuery = mockk<Query>()
        val subQuery2 = mockk<Query>()

        every { mockFirestore.collection("substitutions") } returns subsCollection
        every { subsCollection.whereEqualTo("teamId", "team-doc-id") } returns subsQuery
        every { subsQuery.whereEqualTo("matchId", 1L) } returns subQuery2
        every { subQuery2.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getMatchSubstitutions(1L).test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(emptyList<PlayerSubstitution>(), result)
            cancel()
        }
    }
}
