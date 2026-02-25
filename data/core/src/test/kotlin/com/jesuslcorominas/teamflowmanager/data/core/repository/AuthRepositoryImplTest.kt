package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var authDataSource: AuthDataSource
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        authDataSource = mockk(relaxed = true)
        repository = AuthRepositoryImpl(authDataSource)
    }

    private fun createUser(
        id: String = "user-123",
        email: String? = "user@example.com",
        displayName: String? = "Test User",
        photoUrl: String? = null,
    ) = User(id = id, email = email, displayName = displayName, photoUrl = photoUrl)

    // --- getCurrentUser ---

    @Test
    fun `givenAuthenticatedUser_whenGetCurrentUser_thenReturnsUser`() = runTest {
        val user = createUser()
        every { authDataSource.getCurrentUser() } returns flowOf(user)

        val result = repository.getCurrentUser().first()

        assertEquals(user, result)
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetCurrentUser_thenReturnsNull`() = runTest {
        every { authDataSource.getCurrentUser() } returns flowOf(null)

        val result = repository.getCurrentUser().first()

        assertNull(result)
    }

    // --- signInWithGoogle ---

    @Test
    fun `givenValidIdToken_whenSignInWithGoogle_thenReturnsSuccessWithUser`() = runTest {
        val idToken = "valid-google-id-token"
        val user = createUser()
        coEvery { authDataSource.signInWithGoogle(idToken) } returns Result.success(user)

        val result = repository.signInWithGoogle(idToken)

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        coVerify { authDataSource.signInWithGoogle(idToken) }
    }

    @Test
    fun `givenInvalidIdToken_whenSignInWithGoogle_thenReturnsFailure`() = runTest {
        val idToken = "invalid-token"
        val exception = RuntimeException("Authentication failed")
        coEvery { authDataSource.signInWithGoogle(idToken) } returns Result.failure(exception)

        val result = repository.signInWithGoogle(idToken)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    // --- signOut ---

    @Test
    fun `givenAuthenticatedSession_whenSignOut_thenDelegatesToDataSource`() = runTest {
        repository.signOut()

        coVerify { authDataSource.signOut() }
    }

    // --- saveUserToFirestore ---

    @Test
    fun `givenUser_whenSaveUserToFirestore_thenDelegatesToDataSource`() = runTest {
        val user = createUser()

        repository.saveUserToFirestore(user)

        coVerify { authDataSource.saveUserToFirestore(user) }
    }

    @Test
    fun `givenUserWithNullFields_whenSaveUserToFirestore_thenDelegatesToDataSource`() = runTest {
        val user = createUser(email = null, displayName = null, photoUrl = null)

        repository.saveUserToFirestore(user)

        coVerify { authDataSource.saveUserToFirestore(user) }
    }
}
