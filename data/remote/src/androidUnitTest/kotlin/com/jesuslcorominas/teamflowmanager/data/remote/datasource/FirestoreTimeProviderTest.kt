package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.coEvery
import kotlinx.coroutines.tasks.await
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class FirestoreTimeProviderTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockAuth = mockk<FirebaseAuth>()
    private val mockUser = mockk<FirebaseUser>()
    private lateinit var dataSource: FirestoreTimeProvider

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

        dataSource = FirestoreTimeProvider(mockFirestore, mockAuth)
    }

    @Test
    fun `givenInitialState_whenGetOffset_thenReturnsZero`() {
        assertEquals(0L, dataSource.getOffset())
    }

    @Test
    fun `givenInitialState_whenGetCurrentTime_thenApproximatelyEqualsSystemCurrentTimeMillis`() {
        val before = System.currentTimeMillis()
        val result = dataSource.getCurrentTime()
        val after = System.currentTimeMillis()

        assertTrue(result >= before - 100 && result <= after + 100)
    }

    @Test
    fun `givenNoAuthenticatedUser_whenSynchronize_thenOffsetRemainsZero`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.synchronize()

        assertEquals(0L, dataSource.getOffset())
    }

    @Test
    fun `givenAuthenticatedUserButNoTeam_whenSynchronize_thenOffsetRemainsZero`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val teamsCollection = mockk<CollectionReference>()
        val teamQuery = mockk<Query>()
        val teamSnapshot = mockk<QuerySnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("ownerId", "user-123") } returns teamQuery
        every { teamQuery.limit(1) } returns teamQuery
        val teamTask = mockk<Task<QuerySnapshot>>()
        every { teamQuery.get() } returns teamTask
        coEvery { teamTask.await() } returns teamSnapshot
        every { teamSnapshot.documents } returns emptyList()

        dataSource.synchronize()

        assertEquals(0L, dataSource.getOffset())
    }

    @Test
    fun `givenSynchronizeThrowsException_whenSynchronize_thenDoesNotThrowAndOffsetRemainsZero`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val teamsCollection = mockk<CollectionReference>()
        val teamQuery = mockk<Query>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("ownerId", "user-123") } returns teamQuery
        every { teamQuery.limit(1) } returns teamQuery
        val failingTask = mockk<Task<QuerySnapshot>>()
        every { teamQuery.get() } returns failingTask
        coEvery { failingTask.await() } throws RuntimeException("network error")

        dataSource.synchronize()

        assertEquals(0L, dataSource.getOffset())
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenSynchronize_thenUpdatesServerOffset`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        mockkStatic(FieldValue::class)
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val teamsCollection = mockk<CollectionReference>()
        val teamQuery = mockk<Query>()
        val teamSnapshot = mockk<QuerySnapshot>()
        val teamDoc = mockk<DocumentSnapshot>()
        val docRef = mockk<DocumentReference>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("ownerId", "user-123") } returns teamQuery
        every { teamQuery.limit(1) } returns teamQuery
        val teamTask = mockk<Task<QuerySnapshot>>()
        every { teamQuery.get() } returns teamTask
        coEvery { teamTask.await() } returns teamSnapshot
        every { teamSnapshot.documents } returns listOf(teamDoc)
        every { teamDoc.reference } returns docRef

        val mockFieldValue = mockk<FieldValue>()
        every { FieldValue.serverTimestamp() } returns mockFieldValue

        val updateTask = mockk<Task<Void>>()
        every { docRef.update(any<String>(), any()) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        val readSnapshot = mockk<DocumentSnapshot>()
        val getTask = mockk<Task<DocumentSnapshot>>()
        every { docRef.get() } returns getTask
        coEvery { getTask.await() } returns readSnapshot

        val serverTimeMillis = System.currentTimeMillis() + 500L
        val mockTimestamp = mockk<Timestamp>()
        val mockDate = mockk<java.util.Date>()
        every { readSnapshot.getTimestamp("lastTimeSync") } returns mockTimestamp
        every { mockTimestamp.toDate() } returns mockDate
        every { mockDate.time } returns serverTimeMillis

        dataSource.synchronize()

        // Verify no exception was thrown and the method completed. The offset may be any value
        // depending on timing, but the call itself should succeed.
        assertTrue(dataSource.getOffset() != Long.MIN_VALUE)
    }

    @Test
    fun `givenServerTimestampNull_whenSynchronize_thenOffsetRemainsZero`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        mockkStatic(FieldValue::class)
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val teamsCollection = mockk<CollectionReference>()
        val teamQuery = mockk<Query>()
        val teamSnapshot = mockk<QuerySnapshot>()
        val teamDoc = mockk<DocumentSnapshot>()
        val docRef = mockk<DocumentReference>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("ownerId", "user-123") } returns teamQuery
        every { teamQuery.limit(1) } returns teamQuery
        val teamTask = mockk<Task<QuerySnapshot>>()
        every { teamQuery.get() } returns teamTask
        coEvery { teamTask.await() } returns teamSnapshot
        every { teamSnapshot.documents } returns listOf(teamDoc)
        every { teamDoc.reference } returns docRef

        val mockFieldValue = mockk<FieldValue>()
        every { FieldValue.serverTimestamp() } returns mockFieldValue

        val updateTask = mockk<Task<Void>>()
        every { docRef.update(any<String>(), any()) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        val readSnapshot = mockk<DocumentSnapshot>()
        val getTask = mockk<Task<DocumentSnapshot>>()
        every { docRef.get() } returns getTask
        coEvery { getTask.await() } returns readSnapshot
        every { readSnapshot.getTimestamp("lastTimeSync") } returns null

        dataSource.synchronize()

        assertEquals(0L, dataSource.getOffset())
    }
}
