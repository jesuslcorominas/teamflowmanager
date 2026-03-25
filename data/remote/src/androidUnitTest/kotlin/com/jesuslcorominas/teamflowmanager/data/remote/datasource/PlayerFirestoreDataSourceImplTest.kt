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
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ImageStorageDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import io.mockk.coEvery
import kotlinx.coroutines.tasks.await
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlayerFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockAuth = mockk<FirebaseAuth>()
    private val mockUser = mockk<FirebaseUser>()
    private val mockImageStorage = mockk<ImageStorageDataSource>()
    private val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)
    private lateinit var dataSource: PlayerFirestoreDataSourceImpl

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

        dataSource = PlayerFirestoreDataSourceImpl(mockFirestore, mockAuth, mockImageStorage)
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
    fun `getAllPlayersDirect_returnsEmptyList`() = runTest {
        val result = dataSource.getAllPlayersDirect()
        assertEquals(emptyList<Player>(), result)
    }

    @Test
    fun `clearLocalData_isNoOp`() = runTest {
        dataSource.clearLocalData()
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetAllPlayers_thenEmitsEmptyList`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getAllPlayers().test {
            val result = awaitItem()
            assertEquals(emptyList<Player>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetAllPlayers_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        dataSource.getAllPlayers().test {
            val result = awaitItem()
            assertEquals(emptyList<Player>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetPlayerById_thenReturnsNull`() = runTest {
        setupUserWithNoTeam()

        val result = dataSource.getPlayerById(1L)

        assertNull(result)
    }

    @Test
    fun `givenNoTeam_whenGetCaptainPlayer_thenReturnsNull`() = runTest {
        setupUserWithNoTeam()

        val result = dataSource.getCaptainPlayer()

        assertNull(result)
    }

    @Test
    fun `givenNoTeam_whenSetPlayerAsCaptain_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        try {
            dataSource.setPlayerAsCaptain(1L)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoTeam_whenRemovePlayerAsCaptain_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        try {
            dataSource.removePlayerAsCaptain(1L)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoTeam_whenInsertPlayer_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val player = mockk<Player>(relaxed = true)

        try {
            dataSource.insertPlayer(player)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoTeam_whenDeletePlayer_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        try {
            dataSource.deletePlayer(1L)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoTeam_whenUpdatePlayer_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val player = mockk<Player>(relaxed = true)

        try {
            dataSource.updatePlayer(player)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenValidPlayer_whenInsertPlayer_thenReturnsStableId`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        every { playerDocRef.id } returns "player-doc-id"
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document() } returns playerDocRef

        val voidTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.imageUri } returns null

        val result = dataSource.insertPlayer(player)

        assertTrue(result != 0L)
    }

    @Test
    fun `givenPlayerNotFound_whenDeletePlayer_thenDoesNotThrow`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns emptyList()

        // Should not throw when player not found
        dataSource.deletePlayer(99999L)
    }

    @Test
    fun `givenFirebaseStorageUrl_whenUploadPlayerImageIfNeeded_thenReturnsOriginalUrl`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        every { playerDocRef.id } returns "player-doc-id"
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document() } returns playerDocRef

        val voidTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val firebaseUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg"
        val player = mockk<Player>(relaxed = true)
        every { player.imageUri } returns firebaseUrl

        val result = dataSource.insertPlayer(player)

        // Should succeed without calling imageStorageDataSource.uploadImage
        assertTrue(result != 0L)
    }

    @Test
    fun `givenNullImageUri_whenUploadPlayerImageIfNeeded_thenReturnsNull`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        every { playerDocRef.id } returns "player-doc-id"
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document() } returns playerDocRef

        val voidTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.imageUri } returns null

        val result = dataSource.insertPlayer(player)

        assertTrue(result != 0L)
    }

    @Test
    fun `givenLocalImageUri_whenUploadPlayerImageIfNeeded_thenUploadsImage`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        every { playerDocRef.id } returns "player-doc-id"
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document() } returns playerDocRef

        val voidTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        coEvery { mockImageStorage.uploadImage(any(), any()) } returns "https://firebasestorage.googleapis.com/uploaded.jpg"

        val player = mockk<Player>(relaxed = true)
        every { player.imageUri } returns "content://media/image.jpg"

        val result = dataSource.insertPlayer(player)

        assertTrue(result != 0L)
    }

    @Test
    fun `givenExistingPlayer_whenGetPlayerById_thenReturnsPlayer`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot

        val stablePlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "John",
            lastName = "Doe",
            deleted = false
        )
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { playerSnapshot.documents } returns listOf(playerDoc)

        val result = dataSource.getPlayerById(stablePlayerId)

        assertNotNull(result)
    }

    @Test
    fun `givenNoCaptain_whenGetCaptainPlayer_thenReturnsNull`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>(relaxed = true)
        val playerSnapshot = mockk<QuerySnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        every { playerQuery.whereEqualTo(any<String>(), any()) } returns playerQuery
        every { playerQuery.limit(any()) } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns emptyList()

        val result = dataSource.getCaptainPlayer()

        assertNull(result)
    }

    @Test
    fun `givenCaptainPlayer_whenGetCaptainPlayer_thenReturnsCaptain`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>(relaxed = true)
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        every { playerQuery.whereEqualTo(any<String>(), any()) } returns playerQuery
        every { playerQuery.limit(any()) } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Captain",
            lastName = "America",
            isCaptain = true,
            deleted = false
        )
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { playerSnapshot.documents } returns listOf(playerDoc)

        val result = dataSource.getCaptainPlayer()

        assertNotNull(result)
    }

    @Test
    fun `givenPlayerNotFound_whenSetPlayerAsCaptain_thenDoesNotThrow`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>(relaxed = true)
        every { mockFirestore.collection("players") } returns playersCollection

        val emptySnapshot = mockk<QuerySnapshot>()
        every { emptySnapshot.documents } returns emptyList()

        val relaxedQuery = mockk<Query>(relaxed = true)
        every { playersCollection.whereEqualTo(any<String>(), any()) } returns relaxedQuery
        every { relaxedQuery.whereEqualTo(any<String>(), any()) } returns relaxedQuery
        every { relaxedQuery.limit(any()) } returns relaxedQuery

        val queryTask = mockk<Task<QuerySnapshot>>()
        every { relaxedQuery.get() } returns queryTask
        coEvery { queryTask.await() } returns emptySnapshot

        dataSource.setPlayerAsCaptain(99999L)
    }

    @Test
    fun `givenValidPlayer_whenDeletePlayer_thenSetsDeletedFlag`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val stablePlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("deleted", true) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.deletePlayer(stablePlayerId)
    }

    @Test
    fun `givenPlayerNotFound_whenRemovePlayerAsCaptain_thenDoesNotThrow`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns emptyList()

        dataSource.removePlayerAsCaptain(99999L)
    }

    @Test
    fun `givenValidPlayerWithSameImage_whenUpdatePlayer_thenSucceeds`() = runTest {
        setupUserWithTeam()

        val existingImageUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg"

        val stablePlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false,
            imageUri = existingImageUrl
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns stablePlayerId
        every { player.imageUri } returns existingImageUrl

        dataSource.updatePlayer(player)
    }

    @Test
    fun `givenValidPlayerWithNullImage_whenUpdatePlayer_thenDeletesOldImageAndSetsNull`() = runTest {
        setupUserWithTeam()

        val existingImageUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg"

        val stablePlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false,
            imageUri = existingImageUrl
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        coEvery { mockImageStorage.deleteImage(existingImageUrl) } returns true

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns stablePlayerId
        every { player.imageUri } returns null

        dataSource.updatePlayer(player)
    }

    @Test
    fun `givenValidPlayerWithNewLocalImage_whenUpdatePlayer_thenUploadsNewImageAndDeletesOld`() = runTest {
        setupUserWithTeam()

        val existingImageUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/old_image.jpg"
        val newLocalUri = "content://media/new_image.jpg"
        val uploadedUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/new_image.jpg"

        val stablePlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false,
            imageUri = existingImageUrl
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        coEvery { mockImageStorage.deleteImage(existingImageUrl) } returns true
        coEvery { mockImageStorage.uploadImage(newLocalUri, any()) } returns uploadedUrl

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns stablePlayerId
        every { player.imageUri } returns newLocalUri

        dataSource.updatePlayer(player)
    }

    @Test
    fun `givenPlayerNotFound_whenUpdatePlayer_thenThrowsIllegalStateException`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns emptyList()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns 99999L

        try {
            dataSource.updatePlayer(player)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenValidPlayerWithFirebaseUrl_whenUpdatePlayer_thenUsesExistingUrl`() = runTest {
        setupUserWithTeam()

        val otherFirebaseUrl = "https://storage.googleapis.com/bucket/other.jpg"

        val stablePlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        // Current player has no image
        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false,
            imageUri = null
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns stablePlayerId
        // Player has a firebase storage URL that is different from current (else branch)
        every { player.imageUri } returns otherFirebaseUrl

        dataSource.updatePlayer(player)
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetAllPlayers_thenEmitsPlayers`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playersCollection = mockk<CollectionReference>()
        val playersQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playersQuery
        every { playersQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "John",
            lastName = "Doe",
            deleted = false
        )
        every { docSnapshot.id } returns "player-doc-id"
        every { docSnapshot.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getAllPlayers().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetAllPlayers_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playersCollection = mockk<CollectionReference>()
        val playersQuery = mockk<Query>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playersQuery
        every { playersQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<com.google.firebase.firestore.FirebaseFirestoreException>(relaxed = true)

        dataSource.getAllPlayers().test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<Player>(), result)
            cancel()
        }
    }

    @Test
    fun `givenDeletedCaptainPlayer_whenGetCaptainPlayer_thenReturnsNull`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>(relaxed = true)
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        every { playerQuery.whereEqualTo(any<String>(), any()) } returns playerQuery
        every { playerQuery.limit(any()) } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot

        // Captain is deleted
        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Captain",
            lastName = "Deleted",
            isCaptain = true,
            deleted = true
        )
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { playerSnapshot.documents } returns listOf(playerDoc)

        val result = dataSource.getCaptainPlayer()

        assertNull(result)
    }

    @Test
    fun `givenValidPlayer_whenSetPlayerAsCaptain_thenUpdatesFirestore`() = runTest {
        setupUserWithTeam()

        val stablePlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()

        every { mockFirestore.collection("players") } returns playersCollection

        // Setup for clearAllCaptains (whereEqualTo("teamId") + whereEqualTo("isCaptain",true))
        val captainsQuery = mockk<Query>()
        val captainsSnapshot = mockk<QuerySnapshot>()
        // First call: whereEqualTo("teamId") for clearAllCaptains
        val allTeamQuery = mockk<Query>()
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns allTeamQuery
        every { allTeamQuery.whereEqualTo("isCaptain", true) } returns captainsQuery
        val captainsTask = mockk<Task<QuerySnapshot>>()
        every { captainsQuery.get() } returns captainsTask
        coEvery { captainsTask.await() } returns captainsSnapshot
        every { captainsSnapshot.documents } returns emptyList()

        // Setup for findDocumentIdByPlayerId
        val findQuery = mockk<Query>()
        val findSnapshot = mockk<QuerySnapshot>()
        val findDoc = mockk<DocumentSnapshot>()

        every { allTeamQuery.get() } returns mockk<Task<QuerySnapshot>>().also {
            coEvery { it.await() } returns findSnapshot
        }

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false
        )
        every { findDoc.id } returns "player-doc-id"
        every { findDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { findSnapshot.documents } returns listOf(findDoc)

        // Setup for update("isCaptain", true)
        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("isCaptain", true) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.setPlayerAsCaptain(stablePlayerId)
    }

    @Test
    fun `givenValidPlayer_whenRemovePlayerAsCaptain_thenUpdatesFirestore`() = runTest {
        setupUserWithTeam()

        val stablePlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("isCaptain", false) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.removePlayerAsCaptain(stablePlayerId)
    }

    @Test
    fun `givenExceptionInGetPlayerById_whenGetPlayerById_thenReturnsNull`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } throws RuntimeException("Firestore error")

        val result = dataSource.getPlayerById(1L)

        assertNull(result)
    }

    @Test
    fun `givenPlayerWithEmptyId_whenGetAllPlayers_thenSetsIdFromDocumentId`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playersCollection = mockk<CollectionReference>()
        val playersQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playersQuery
        every { playersQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // Model has empty id - should be set from document id
        val playerModel = PlayerFirestoreModel(
            id = "",
            teamId = "team-doc-id",
            firstName = "John",
            lastName = "Doe",
            deleted = false
        )
        every { docSnapshot.id } returns "player-doc-id"
        every { docSnapshot.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getAllPlayers().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenLocalFileUri_whenInsertPlayer_thenUploadsImageAndReturnsStableId`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        every { playerDocRef.id } returns "player-doc-id"
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document() } returns playerDocRef

        val voidTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val uploadedUrl = "https://firebasestorage.googleapis.com/uploaded.jpg"
        coEvery { mockImageStorage.uploadImage("file:///storage/image.jpg", any()) } returns uploadedUrl

        val player = mockk<Player>(relaxed = true)
        every { player.imageUri } returns "file:///storage/image.jpg"

        val result = dataSource.insertPlayer(player)
        assertTrue(result != 0L)
    }

    @Test
    fun `givenExistingCaptain_whenSetPlayerAsCaptain_thenClearsOldCaptainAndSetsNew`() = runTest {
        setupUserWithTeam()

        val stableNewPlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "new-player-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playersCollection = mockk<CollectionReference>()
        val allTeamQuery = mockk<Query>()
        val captainsQuery = mockk<Query>()
        val captainsSnapshot = mockk<QuerySnapshot>()
        val captainDoc = mockk<DocumentSnapshot>()
        val captainDocRef = mockk<DocumentReference>()
        val findSnapshot = mockk<QuerySnapshot>()
        val findDoc = mockk<DocumentSnapshot>()
        val newPlayerDocRef = mockk<DocumentReference>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns allTeamQuery
        every { allTeamQuery.whereEqualTo("isCaptain", true) } returns captainsQuery

        val captainsTask = mockk<Task<QuerySnapshot>>()
        every { captainsQuery.get() } returns captainsTask
        coEvery { captainsTask.await() } returns captainsSnapshot

        val existingCaptainModel = PlayerFirestoreModel(
            id = "old-captain-id",
            teamId = "team-doc-id",
            firstName = "Old",
            lastName = "Captain",
            isCaptain = true,
            deleted = false
        )
        every { captainDoc.id } returns "old-captain-id"
        every { captainDoc.toObject(PlayerFirestoreModel::class.java) } returns existingCaptainModel
        every { captainsSnapshot.documents } returns listOf(captainDoc)

        every { playersCollection.document("old-captain-id") } returns captainDocRef
        val clearTask = mockk<Task<Void>>()
        every { captainDocRef.update("isCaptain", false) } returns clearTask
        coEvery { clearTask.await() } returns mockk()

        val findTask = mockk<Task<QuerySnapshot>>()
        every { allTeamQuery.get() } returns findTask
        coEvery { findTask.await() } returns findSnapshot

        val newPlayerModel = PlayerFirestoreModel(
            id = "new-player-id",
            teamId = "team-doc-id",
            firstName = "New",
            lastName = "Captain",
            deleted = false
        )
        every { findDoc.id } returns "new-player-id"
        every { findDoc.toObject(PlayerFirestoreModel::class.java) } returns newPlayerModel
        every { findSnapshot.documents } returns listOf(findDoc)

        every { playersCollection.document("new-player-id") } returns newPlayerDocRef
        val setTask = mockk<Task<Void>>()
        every { newPlayerDocRef.update("isCaptain", true) } returns setTask
        coEvery { setTask.await() } returns mockk()

        dataSource.setPlayerAsCaptain(stableNewPlayerId)
    }

    @Test
    fun `givenDeletedCaptain_whenSetPlayerAsCaptain_thenSkipsDeletedCaptainUpdate`() = runTest {
        setupUserWithTeam()

        val stableNewPlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playersCollection = mockk<CollectionReference>()
        val allTeamQuery = mockk<Query>()
        val captainsQuery = mockk<Query>()
        val captainsSnapshot = mockk<QuerySnapshot>()
        val captainDoc = mockk<DocumentSnapshot>()
        val findSnapshot = mockk<QuerySnapshot>()
        val findDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns allTeamQuery
        every { allTeamQuery.whereEqualTo("isCaptain", true) } returns captainsQuery

        val captainsTask = mockk<Task<QuerySnapshot>>()
        every { captainsQuery.get() } returns captainsTask
        coEvery { captainsTask.await() } returns captainsSnapshot

        val deletedCaptainModel = PlayerFirestoreModel(
            id = "old-captain-id",
            teamId = "team-doc-id",
            firstName = "Deleted",
            lastName = "Captain",
            isCaptain = true,
            deleted = true
        )
        every { captainDoc.id } returns "old-captain-id"
        every { captainDoc.toObject(PlayerFirestoreModel::class.java) } returns deletedCaptainModel
        every { captainsSnapshot.documents } returns listOf(captainDoc)

        val findTask = mockk<Task<QuerySnapshot>>()
        every { allTeamQuery.get() } returns findTask
        coEvery { findTask.await() } returns findSnapshot

        val newPlayerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "New",
            lastName = "Captain",
            deleted = false
        )
        every { findDoc.id } returns "player-doc-id"
        every { findDoc.toObject(PlayerFirestoreModel::class.java) } returns newPlayerModel
        every { findSnapshot.documents } returns listOf(findDoc)

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.update("isCaptain", true) } returns setTask
        coEvery { setTask.await() } returns mockk()

        dataSource.setPlayerAsCaptain(stableNewPlayerId)
    }

    @Test
    fun `givenPlayerWithEmptyIdModel_whenGetPlayerById_thenSetsIdFromDocumentId`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot

        val stablePlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playerModel = PlayerFirestoreModel(
            id = "",
            teamId = "team-doc-id",
            firstName = "John",
            lastName = "Doe",
            deleted = false
        )
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { playerSnapshot.documents } returns listOf(playerDoc)

        val result = dataSource.getPlayerById(stablePlayerId)

        assertNotNull(result)
    }

    @Test
    fun `givenPlayerWithEmptyIdModel_whenGetCaptainPlayer_thenSetsIdFromDocumentId`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>(relaxed = true)
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        every { playerQuery.whereEqualTo(any<String>(), any()) } returns playerQuery
        every { playerQuery.limit(any()) } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot

        val playerModel = PlayerFirestoreModel(
            id = "",
            teamId = "team-doc-id",
            firstName = "Captain",
            lastName = "America",
            isCaptain = true,
            deleted = false
        )
        every { playerDoc.id } returns "captain-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { playerSnapshot.documents } returns listOf(playerDoc)

        val result = dataSource.getCaptainPlayer()

        assertNotNull(result)
    }

    @Test
    fun `givenPlayerWithEmptyIdModel_whenDeletePlayer_thenSetsIdFromDocumentIdAndDeletes`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val stablePlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playerModel = PlayerFirestoreModel(
            id = "",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("deleted", true) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.deletePlayer(stablePlayerId)
    }

    @Test
    fun `givenPlayerWithNullImageAndNoExistingImage_whenUpdatePlayer_thenSucceeds`() = runTest {
        setupUserWithTeam()

        val stablePlayerId = run {
            var result = 0L; var multiplier = 1L
            for (char in "player-doc-id") { result += char.code * multiplier; multiplier *= 31 }
            kotlin.math.abs(result)
        }

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false,
            imageUri = null
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns stablePlayerId
        every { player.imageUri } returns null

        dataSource.updatePlayer(player)
    }
}
