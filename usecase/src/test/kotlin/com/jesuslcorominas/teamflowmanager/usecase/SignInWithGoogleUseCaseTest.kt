package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignInWithGoogleUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        signInWithGoogleUseCase = SignInWithGoogleUseCaseImpl(authRepository)
    }

    @Test
    fun `invoke should return success and save user when sign in succeeds`() = runTest {
        // Given
        val idToken = "google_id_token"
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://example.com/photo.jpg"
        )
        coEvery { authRepository.signInWithGoogle(idToken) } returns Result.success(user)

        // When
        val result = signInWithGoogleUseCase.invoke(idToken)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        coVerify { authRepository.signInWithGoogle(idToken) }
        coVerify { authRepository.saveUserToFirestore(user) }
    }

    @Test
    fun `invoke should return failure when sign in fails`() = runTest {
        // Given
        val idToken = "google_id_token"
        val exception = Exception("Sign in failed")
        coEvery { authRepository.signInWithGoogle(idToken) } returns Result.failure(exception)

        // When
        val result = signInWithGoogleUseCase.invoke(idToken)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { authRepository.signInWithGoogle(idToken) }
        coVerify(exactly = 0) { authRepository.saveUserToFirestore(any()) }
    }
}
