package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class FirebaseAuthDataSourceImplTest {

    private val mockAuth = mockk<FirebaseAuth>()
    private val mockUser = mockk<FirebaseUser>()
    private lateinit var dataSource: FirebaseAuthDataSourceImpl

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
        every { Log.i(any(), any()) } returns 0

        dataSource = FirebaseAuthDataSourceImpl(mockAuth)
    }

    @Test
    fun `givenAuthenticatedUser_whenGetCurrentUser_thenEmitsUser`() = runTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        every { mockAuth.addAuthStateListener(capture(listenerSlot)) } answers {}
        every { mockAuth.removeAuthStateListener(any()) } answers {}
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "uid-123"
        every { mockUser.email } returns "test@test.com"
        every { mockUser.displayName } returns "Test User"
        every { mockUser.photoUrl } returns null

        dataSource.getCurrentUser().test {
            listenerSlot.captured.onAuthStateChanged(mockAuth)
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("uid-123", result!!.id)
            assertEquals("test@test.com", result.email)
            assertEquals("Test User", result.displayName)
            cancel()
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetCurrentUser_thenEmitsNull`() = runTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        every { mockAuth.addAuthStateListener(capture(listenerSlot)) } answers {}
        every { mockAuth.removeAuthStateListener(any()) } answers {}
        every { mockAuth.currentUser } returns null

        dataSource.getCurrentUser().test {
            listenerSlot.captured.onAuthStateChanged(mockAuth)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenValidIdToken_whenSignInWithGoogle_thenReturnsSuccess`() = runTest {
        mockkStatic(GoogleAuthProvider::class)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val mockCredential = mockk<com.google.firebase.auth.AuthCredential>()
        every { GoogleAuthProvider.getCredential("valid-token", null) } returns mockCredential

        val mockTask = mockk<Task<AuthResult>>()
        every { mockAuth.signInWithCredential(mockCredential) } returns mockTask

        val mockAuthResult = mockk<AuthResult>()
        coEvery { mockTask.await() } returns mockAuthResult
        every { mockAuthResult.user } returns mockUser
        every { mockUser.uid } returns "uid-123"
        every { mockUser.email } returns "test@test.com"
        every { mockUser.displayName } returns "Test User"
        every { mockUser.photoUrl } returns null

        val result = dataSource.signInWithGoogle("valid-token")

        assertTrue(result.isSuccess)
        assertEquals("uid-123", result.getOrNull()?.id)
    }

    @Test
    fun `givenSignInThrowsException_whenSignInWithGoogle_thenReturnsFailure`() = runTest {
        mockkStatic(GoogleAuthProvider::class)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val mockCredential = mockk<com.google.firebase.auth.AuthCredential>()
        every { GoogleAuthProvider.getCredential("invalid-token", null) } returns mockCredential

        val mockTask = mockk<Task<AuthResult>>()
        every { mockAuth.signInWithCredential(mockCredential) } returns mockTask
        coEvery { mockTask.await() } throws RuntimeException("Sign in failed")

        val result = dataSource.signInWithGoogle("invalid-token")

        assertTrue(result.isFailure)
    }

    @Test
    fun `givenNullUserAfterSignIn_whenSignInWithGoogle_thenReturnsFailure`() = runTest {
        mockkStatic(GoogleAuthProvider::class)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val mockCredential = mockk<com.google.firebase.auth.AuthCredential>()
        every { GoogleAuthProvider.getCredential("token-null-user", null) } returns mockCredential

        val mockTask = mockk<Task<AuthResult>>()
        every { mockAuth.signInWithCredential(mockCredential) } returns mockTask

        val mockAuthResult = mockk<AuthResult>()
        coEvery { mockTask.await() } returns mockAuthResult
        every { mockAuthResult.user } returns null

        val result = dataSource.signInWithGoogle("token-null-user")

        assertTrue(result.isFailure)
    }

    @Test
    fun `whenSignOut_thenCallsFirebaseSignOut`() = runTest {
        every { mockAuth.signOut() } returns Unit

        dataSource.signOut()

        verify(exactly = 1) { mockAuth.signOut() }
    }

    @Test
    fun `givenUser_whenSaveUserToFirestore_thenDoesNothing`() = runTest {
        val user = com.jesuslcorominas.teamflowmanager.domain.model.User(
            id = "uid-123",
            email = "test@test.com",
            displayName = "Test User",
            photoUrl = null
        )

        // Should not throw - it's a no-op
        dataSource.saveUserToFirestore(user)
    }
}
