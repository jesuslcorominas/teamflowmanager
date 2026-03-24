package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.coEvery
import kotlinx.coroutines.tasks.await
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class FirebaseStorageDataSourceImplTest {

    private val mockStorage = mockk<FirebaseStorage>()
    private lateinit var dataSource: FirebaseStorageDataSourceImpl

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

        dataSource = FirebaseStorageDataSourceImpl(mockStorage)
    }

    @Test
    fun `givenLocalUri_whenUploadImage_thenReturnsDownloadUrl`() = runTest {
        mockkStatic(Uri::class)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val mockUri = mockk<Uri>()
        every { Uri.parse(any()) } returns mockUri

        val mockStorageRef = mockk<StorageReference>(relaxed = true)
        every { mockStorage.reference } returns mockStorageRef
        every { mockStorageRef.child(any()) } returns mockStorageRef

        val mockUploadTask = mockk<UploadTask>()
        every { mockStorageRef.putFile(mockUri) } returns mockUploadTask

        val mockDownloadUriTask = mockk<Task<Uri>>()
        every { mockStorageRef.downloadUrl } returns mockDownloadUriTask

        val mockDownloadUri = mockk<Uri>()
        every { mockDownloadUri.toString() } returns "https://firebase-download-url.com/image.jpg"

        coEvery { mockUploadTask.await() } returns mockk()
        coEvery { mockDownloadUriTask.await() } returns mockDownloadUri

        val result = dataSource.uploadImage("content://local/image.jpg", "players_images/uid/player.jpg")

        assertEquals("https://firebase-download-url.com/image.jpg", result)
    }

    @Test
    fun `givenStorageException_whenUploadImage_thenReturnsNull`() = runTest {
        mockkStatic(Uri::class)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val mockUri = mockk<Uri>()
        every { Uri.parse(any()) } returns mockUri

        val mockStorageRef = mockk<StorageReference>(relaxed = true)
        every { mockStorage.reference } returns mockStorageRef
        every { mockStorageRef.child(any()) } returns mockStorageRef

        val mockUploadTask = mockk<UploadTask>()
        every { mockStorageRef.putFile(mockUri) } returns mockUploadTask
        coEvery { mockUploadTask.await() } throws RuntimeException("Storage error")

        val result = dataSource.uploadImage("content://local/image.jpg", "players_images/uid/player.jpg")

        assertNull(result)
    }

    @Test
    fun `givenValidDownloadUrl_whenDeleteImage_thenReturnsTrue`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val mockStorageRef = mockk<StorageReference>(relaxed = true)
        every { mockStorage.getReferenceFromUrl(any()) } returns mockStorageRef

        val mockDeleteTask = mockk<Task<Void>>()
        every { mockStorageRef.delete() } returns mockDeleteTask
        coEvery { mockDeleteTask.await() } returns mockk()

        val result = dataSource.deleteImage("https://firebasestorage.googleapis.com/image.jpg")

        assertTrue(result)
    }

    @Test
    fun `givenStorageException_whenDeleteImage_thenReturnsFalse`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val mockStorageRef = mockk<StorageReference>(relaxed = true)
        every { mockStorage.getReferenceFromUrl(any()) } returns mockStorageRef

        val mockDeleteTask = mockk<Task<Void>>()
        every { mockStorageRef.delete() } returns mockDeleteTask
        coEvery { mockDeleteTask.await() } throws RuntimeException("Delete error")

        val result = dataSource.deleteImage("https://firebasestorage.googleapis.com/image.jpg")

        assertFalse(result)
    }
}
