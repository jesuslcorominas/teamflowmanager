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
import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import io.mockk.coEvery
import kotlinx.coroutines.tasks.await
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test

class GoalFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockAuth = mockk<FirebaseAuth>()
    private val mockUser = mockk<FirebaseUser>()
    private val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)
    private lateinit var dataSource: GoalFirestoreDataSourceImpl

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

        dataSource = GoalFirestoreDataSourceImpl(mockFirestore, mockAuth)
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
    fun `getAllGoalsDirect_returnsEmptyList`() = runTest {
        val result = dataSource.getAllGoalsDirect()
        assertEquals(emptyList<Goal>(), result)
    }

    @Test
    fun `clearLocalData_isNoOp`() = runTest {
        // Should not throw
        dataSource.clearLocalData()
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetMatchGoals_thenEmitsEmptyList`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getMatchGoals("1").test {
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetMatchGoals_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        dataSource.getMatchGoals("1").test {
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetAllTeamGoals_thenEmitsEmptyList`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getAllTeamGoals().test {
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenInsertGoal_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val goal = mockk<Goal>(relaxed = true)

        try {
            dataSource.insertGoal(goal)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoTeam_whenGetAllTeamGoals_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        dataSource.getAllTeamGoals().test {
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetMatchGoals_thenEmitsList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQueryByTeam = mockk<Query>()
        val goalsQueryNew = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQueryByTeam
        every { goalsQueryByTeam.whereEqualTo("matchId", "1") } returns goalsQueryNew
        every { goalsQueryNew.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        every { docSnapshot.data } returns mapOf("matchId" to "1", "teamId" to "team-doc-id", "scorerId" to null)
        every { docSnapshot.id } returns "goal-doc-id"
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getMatchGoals("1").test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetMatchGoals_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQueryByTeam = mockk<Query>()
        val goalsQueryNew = mockk<Query>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQueryByTeam
        every { goalsQueryByTeam.whereEqualTo("matchId", "1") } returns goalsQueryNew
        every { goalsQueryNew.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getMatchGoals("1").test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetAllTeamGoals_thenEmitsList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQuery
        every { goalsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        every { docSnapshot.data } returns mapOf("matchId" to "1", "teamId" to "team-doc-id", "scorerId" to null)
        every { docSnapshot.id } returns "goal-doc-id"
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getAllTeamGoals().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetAllTeamGoals_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQuery = mockk<Query>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQuery
        every { goalsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getAllTeamGoals().test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenRuntimeException_whenInsertGoal_thenPropagatesException`() = runTest {
        setupUserWithTeam()

        val goalsCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val goalDocRef = mockk<DocumentReference>()
        every { goalDocRef.id } returns "goal-doc-id"

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { goalsCollection.document() } returns goalDocRef

        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        val voidTask = mockk<Task<Void>>()
        every { goalDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } throws RuntimeException("Firestore error")

        val goal = mockk<Goal>(relaxed = true)
        every { goal.matchId } returns "1"

        try {
            dataSource.insertGoal(goal)
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            // expected
        }
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenInsertGoal_thenReturnsStableId`() = runTest {
        setupUserWithTeam()

        val goalsCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val goalDocRef = mockk<DocumentReference>()
        every { goalDocRef.id } returns "goal-doc-id"

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { goalsCollection.document() } returns goalDocRef

        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        val voidTask = mockk<Task<Void>>()
        every { goalDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val goal = mockk<Goal>(relaxed = true)
        every { goal.matchId } returns "1"

        val result = dataSource.insertGoal(goal)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `givenMatchFoundInFirestore_whenInsertGoal_thenUsesMatchDocId`() = runTest {
        setupUserWithTeam()

        val goalsCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val goalDocRef = mockk<DocumentReference>()
        every { goalDocRef.id } returns "goal-doc-id"

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { goalsCollection.document() } returns goalDocRef

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
        every { goalDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val goal = mockk<Goal>(relaxed = true)
        every { goal.matchId } returns "match-doc-id"

        val result = dataSource.insertGoal(goal)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `givenExceptionDuringSet_whenInsertGoal_thenPropagatesException`() = runTest {
        setupUserWithTeam()

        val goalsCollection = mockk<CollectionReference>()
        val matchesCollection = mockk<CollectionReference>()
        val goalDocRef = mockk<DocumentReference>()
        every { goalDocRef.id } returns "goal-doc-id"

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { goalsCollection.document() } returns goalDocRef

        val matchQuery = mockk<Query>()
        val matchSnapshot = mockk<QuerySnapshot>()
        every { matchesCollection.whereEqualTo("teamId", "team-doc-id") } returns matchQuery
        val matchTask = mockk<Task<QuerySnapshot>>()
        every { matchQuery.get() } returns matchTask
        coEvery { matchTask.await() } returns matchSnapshot
        every { matchSnapshot.documents } returns emptyList()

        val voidTask = mockk<Task<Void>>()
        every { goalDocRef.set(any()) } returns voidTask
        // Use a specific exception subclass to cover the FirebaseFirestoreException catch branch
        coEvery { voidTask.await() } throws IllegalStateException("Firestore write failed")

        val goal = mockk<Goal>(relaxed = true)
        every { goal.matchId } returns "1"

        try {
            dataSource.insertGoal(goal)
            fail("Expected exception")
        } catch (e: Exception) {
            // expected
        }
    }

    @Test
    fun `givenNullSnapshot_whenGetMatchGoals_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQueryByTeam = mockk<Query>()
        val goalsQueryNew = mockk<Query>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQueryByTeam
        every { goalsQueryByTeam.whereEqualTo("matchId", "1") } returns goalsQueryNew
        every { goalsQueryNew.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getMatchGoals("1").test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNullSnapshot_whenGetAllTeamGoals_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQuery = mockk<Query>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQuery
        every { goalsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getAllTeamGoals().test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenInsertGoal_thenThrowsIllegalStateException`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns null

        val goal = mockk<Goal>(relaxed = true)

        try {
            dataSource.insertGoal(goal)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenPresidentUser_whenGetMatchGoals_thenUsesMatchDocumentTeamId`() = runTest {
        // President: getTeamDocumentId() returns null, but match document has teamId
        setupUserWithNoTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchDocRef = mockk<DocumentReference>()
        val matchDocTask = mockk<Task<DocumentSnapshot>>()
        val matchDocSnapshot = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.document("1") } returns matchDocRef
        every { matchDocRef.get() } returns matchDocTask
        coEvery { matchDocTask.await() } returns matchDocSnapshot
        every { matchDocSnapshot.getString("teamId") } returns "team-doc-id"

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQueryByTeam = mockk<Query>()
        val goalsQueryNew = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQueryByTeam
        every { goalsQueryByTeam.whereEqualTo("matchId", "1") } returns goalsQueryNew
        every { goalsQueryNew.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        every { docSnapshot.data } returns mapOf("matchId" to "1", "teamId" to "team-doc-id", "scorerId" to null)
        every { docSnapshot.id } returns "goal-doc-id"
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getMatchGoals("1").test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenNullRawData_whenParseGoalDocument_thenReturnsNull`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQueryByTeam = mockk<Query>()
        val goalsQueryNew = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQueryByTeam
        every { goalsQueryByTeam.whereEqualTo("matchId", "1") } returns goalsQueryNew
        every { goalsQueryNew.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        every { docSnapshot.data } returns null
        every { docSnapshot.id } returns "goal-doc-id"
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getMatchGoals("1").test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(0, result.size)
            cancel()
        }
    }

    @Test
    fun `givenGoalDocWithMissingFields_whenParseGoalDocument_thenUsesDefaults`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQueryByTeam = mockk<Query>()
        val goalsQueryNew = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQueryByTeam
        every { goalsQueryByTeam.whereEqualTo("matchId", "1") } returns goalsQueryNew
        every { goalsQueryNew.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // Document with minimal fields (defaults should be used)
        every { docSnapshot.data } returns mapOf("scorerId" to "scorer-123")
        every { docSnapshot.id } returns "goal-doc-id"
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getMatchGoals("1").test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenMatchDocumentWithoutTeamId_whenGetTeamDocumentIdOrFromMatch_thenReturnsNull`() = runTest {
        setupUserWithNoTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchDocRef = mockk<DocumentReference>()
        val matchDocTask = mockk<Task<DocumentSnapshot>>()
        val matchDocSnapshot = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.document("1") } returns matchDocRef
        every { matchDocRef.get() } returns matchDocTask
        coEvery { matchDocTask.await() } returns matchDocSnapshot
        every { matchDocSnapshot.getString("teamId") } returns null

        dataSource.getMatchGoals("1").test {
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenExceptionWhenFetchingMatchDocument_whenGetTeamDocumentIdOrFromMatch_thenReturnsNull`() = runTest {
        setupUserWithNoTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchDocRef = mockk<DocumentReference>()
        val matchDocTask = mockk<Task<DocumentSnapshot>>()
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.document("1") } returns matchDocRef
        every { matchDocRef.get() } returns matchDocTask
        coEvery { matchDocTask.await() } throws Exception("Match fetch failed")

        dataSource.getMatchGoals("1").test {
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenCancellationExceptionDuringInsert_whenInsertGoal_thenPropagatesCancellationException`() = runTest {
        setupUserWithTeam()

        val goalsCollection = mockk<CollectionReference>()
        val goalDocRef = mockk<DocumentReference>()
        every { goalDocRef.id } returns "goal-doc-id"

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.document() } returns goalDocRef

        val voidTask = mockk<Task<Void>>()
        every { goalDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } throws kotlinx.coroutines.CancellationException("Cancelled")

        val goal = mockk<Goal>(relaxed = true)
        every { goal.matchId } returns "1"

        try {
            dataSource.insertGoal(goal)
            fail("Expected CancellationException")
        } catch (e: kotlinx.coroutines.CancellationException) {
            // expected
        }
    }

    @Test
    fun `givenCancellationExceptionDuringTeamLookup_whenInsertGoal_thenPropagatesCancellationException`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"
        val teamsCollection = mockk<CollectionReference>()
        val teamQuery = mockk<Query>()
        val teamTask = mockk<Task<QuerySnapshot>>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamQuery
        every { teamQuery.limit(1) } returns teamQuery
        every { teamQuery.get() } returns teamTask
        coEvery { teamTask.await() } throws kotlinx.coroutines.CancellationException("Cancelled")

        val goal = mockk<Goal>(relaxed = true)

        try {
            dataSource.insertGoal(goal)
            fail("Expected CancellationException")
        } catch (e: kotlinx.coroutines.CancellationException) {
            // expected
        }
    }

    @Test
    fun `givenGoalDocWithEmptyScorerId_whenParseGoalDocument_thenHandlesGracefully`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQueryByTeam = mockk<Query>()
        val goalsQueryNew = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQueryByTeam
        every { goalsQueryByTeam.whereEqualTo("matchId", "1") } returns goalsQueryNew
        every { goalsQueryNew.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // Document with missing scorerId
        every { docSnapshot.data } returns mapOf(
            "matchId" to "1",
            "teamId" to "team-doc-id"
        )
        every { docSnapshot.id } returns "goal-doc-id"
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getMatchGoals("1").test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            // Should still parse goal with null scorerId
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenMultipleValidGoals_whenGetMatchGoals_thenEmitsAllGoals`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQueryByTeam = mockk<Query>()
        val goalsQueryNew = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val firstDoc = mockk<DocumentSnapshot>()
        val secondDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQueryByTeam
        every { goalsQueryByTeam.whereEqualTo("matchId", "1") } returns goalsQueryNew
        every { goalsQueryNew.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        every { firstDoc.data } returns mapOf("matchId" to "1", "teamId" to "team-doc-id", "scorerId" to "scorer-1")
        every { firstDoc.id } returns "goal-1"
        every { secondDoc.data } returns mapOf("matchId" to "1", "teamId" to "team-doc-id", "scorerId" to "scorer-2")
        every { secondDoc.id } returns "goal-2"
        every { querySnapshot.documents } returns listOf(firstDoc, secondDoc)

        dataSource.getMatchGoals("1").test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals(listOf("scorer-1", "scorer-2"), result.map { it.scorerId })
            cancel()
        }
    }

    @Test
    fun `givenNoTeamAndNoMatchDocument_whenGetMatchGoals_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        val matchesCollection = mockk<CollectionReference>()
        val matchDocRef = mockk<DocumentReference>()
        val matchDocTask = mockk<Task<DocumentSnapshot>>()
        val matchDocSnapshot = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("matches") } returns matchesCollection
        every { matchesCollection.document("1") } returns matchDocRef
        every { matchDocRef.get() } returns matchDocTask
        coEvery { matchDocTask.await() } returns matchDocSnapshot
        every { matchDocSnapshot.getString("teamId") } returns null

        dataSource.getMatchGoals("1").test {
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenGoalDocWithOpponentGoalFlag_whenParseGoalDocument_thenParsesCorrectly`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQueryByTeam = mockk<Query>()
        val goalsQueryNew = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQueryByTeam
        every { goalsQueryByTeam.whereEqualTo("matchId", "1") } returns goalsQueryNew
        every { goalsQueryNew.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        every { docSnapshot.data } returns mapOf(
            "matchId" to "1",
            "teamId" to "team-doc-id",
            "scorerId" to "scorer-123",
            "opponentGoal" to true,
            "ownGoal" to false
        )
        every { docSnapshot.id } returns "goal-doc-id"
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getMatchGoals("1").test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenGetAllTeamGoalsWithNullSnapshot_whenAddSnapshotListener_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQuery = mockk<Query>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQuery
        every { goalsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getAllTeamGoals().test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }

    @Test
    fun `givenGetAllTeamGoalsWithException_whenAddSnapshotListener_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val goalsCollection = mockk<CollectionReference>()
        val goalsQuery = mockk<Query>()

        every { mockFirestore.collection("goals") } returns goalsCollection
        every { goalsCollection.whereEqualTo("teamId", "team-doc-id") } returns goalsQuery
        every { goalsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getAllTeamGoals().test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<Goal>(), result)
            cancel()
        }
    }
}
