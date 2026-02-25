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
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.MatchOperationFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import io.mockk.coEvery
import kotlinx.coroutines.tasks.await
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test

class MatchOperationFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockAuth = mockk<FirebaseAuth>()
    private val mockUser = mockk<FirebaseUser>()
    private lateinit var dataSource: MatchOperationFirestoreDataSourceImpl

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

        dataSource = MatchOperationFirestoreDataSourceImpl(mockFirestore, mockAuth)
    }

    // NOTE: MatchOperationFirestoreDataSourceImpl uses "assignedCoachId" (not "ownerId") to find team
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
    fun `givenEmptyOperationId_whenUpdateOperation_thenThrowsIllegalArgumentException`() = runTest {
        setupUserWithTeam()

        val operation = mockk<MatchOperation>(relaxed = true)
        every { operation.id } returns ""

        try {
            dataSource.updateOperation(operation)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenNoTeam_whenCreateOperation_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val operation = mockk<MatchOperation>(relaxed = true)

        try {
            dataSource.createOperation(operation)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoTeam_whenUpdateOperation_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val operation = mockk<MatchOperation>(relaxed = true)
        every { operation.id } returns "op-id"

        try {
            dataSource.updateOperation(operation)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenException_whenGetOperationById_thenReturnsNull`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val operationsCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        every { mockFirestore.collection("matchOperations") } returns operationsCollection
        every { operationsCollection.document("op-id") } returns docRef

        val getTask = mockk<Task<DocumentSnapshot>>()
        every { docRef.get() } returns getTask
        coEvery { getTask.await() } throws RuntimeException("Network error")

        val result = dataSource.getOperationById("op-id")

        assertNull(result)
    }

    @Test
    fun `givenValidOperation_whenCreateOperation_thenReturnsDocumentId`() = runTest {
        setupUserWithTeam()

        val operationsCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        every { docRef.id } returns "new-op-id"
        every { mockFirestore.collection("matchOperations") } returns operationsCollection
        every { operationsCollection.document() } returns docRef

        val voidTask = mockk<Task<Void>>()
        every { docRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val operation = mockk<MatchOperation>(relaxed = true)

        val result = dataSource.createOperation(operation)

        assertEquals("new-op-id", result)
    }

    @Test
    fun `givenExistingOperation_whenGetOperationById_thenReturnsOperation`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val operationsCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        every { mockFirestore.collection("matchOperations") } returns operationsCollection
        every { operationsCollection.document("op-id") } returns docRef

        val docSnapshot = mockk<DocumentSnapshot>()
        val getTask = mockk<Task<DocumentSnapshot>>()
        every { docRef.get() } returns getTask
        coEvery { getTask.await() } returns docSnapshot

        val operationModel = MatchOperationFirestoreModel(
            id = "op-id",
            teamId = "team-doc-id",
            matchId = 1L
        )
        every { docSnapshot.toObject(MatchOperationFirestoreModel::class.java) } returns operationModel

        val result = dataSource.getOperationById("op-id")

        assertNotNull(result)
    }

    @Test
    fun `givenNoModel_whenGetOperationById_thenReturnsNull`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val operationsCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        every { mockFirestore.collection("matchOperations") } returns operationsCollection
        every { operationsCollection.document("op-id") } returns docRef

        val docSnapshot = mockk<DocumentSnapshot>()
        val getTask = mockk<Task<DocumentSnapshot>>()
        every { docRef.get() } returns getTask
        coEvery { getTask.await() } returns docSnapshot
        every { docSnapshot.toObject(MatchOperationFirestoreModel::class.java) } returns null

        val result = dataSource.getOperationById("op-id")

        assertNull(result)
    }

    @Test
    fun `givenNoAuthenticatedUser_whenCreateOperation_thenThrowsIllegalStateException`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns null

        val operation = mockk<MatchOperation>(relaxed = true)

        try {
            dataSource.createOperation(operation)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenValidOperation_whenUpdateOperation_thenSucceeds`() = runTest {
        setupUserWithTeam()

        val operationsCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        every { mockFirestore.collection("matchOperations") } returns operationsCollection
        every { operationsCollection.document("op-id") } returns docRef

        val voidTask = mockk<Task<Void>>()
        every { docRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val operation = mockk<MatchOperation>(relaxed = true)
        every { operation.id } returns "op-id"

        dataSource.updateOperation(operation)
    }

    @Test
    fun `givenNoAuthenticatedUser_whenUpdateOperation_thenThrowsIllegalStateException`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns null

        val operation = mockk<MatchOperation>(relaxed = true)
        every { operation.id } returns "op-id"

        try {
            dataSource.updateOperation(operation)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenFirestoreError_whenCreateOperation_thenPropagatesException`() = runTest {
        setupUserWithTeam()

        val operationsCollection = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        every { docRef.id } returns "new-op-id"
        every { mockFirestore.collection("matchOperations") } returns operationsCollection
        every { operationsCollection.document() } returns docRef

        val voidTask = mockk<Task<Void>>()
        every { docRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } throws RuntimeException("Network error")

        val operation = mockk<MatchOperation>(relaxed = true)

        try {
            dataSource.createOperation(operation)
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            // expected
        }
    }
}
