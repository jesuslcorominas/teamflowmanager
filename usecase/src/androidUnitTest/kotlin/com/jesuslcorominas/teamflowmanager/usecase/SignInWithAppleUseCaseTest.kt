package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
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

class SignInWithAppleUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: SignInWithAppleUseCaseImpl

    private val credential = AppleSignInCredential(idToken = "apple_id_token", rawNonce = "raw_nonce", fullName = "Test User")
    private val user = User(id = "user123", email = "relay@example.com", displayName = "Test User", photoUrl = null)

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        useCase = SignInWithAppleUseCaseImpl(authRepository)
    }

    @Test
    fun `when sign in succeeds, returns the user and saves it`() =
        runTest {
            coEvery { authRepository.signInWithApple(credential) } returns Result.success(user)

            val result = useCase(credential)

            assertTrue(result.isSuccess)
            assertEquals(user, result.getOrNull())
            coVerify { authRepository.saveUserToFirestore(user) }
        }

    @Test
    fun `when sign in fails, propagates the failure without saving`() =
        runTest {
            val error = Exception("Sign in failed")
            coEvery { authRepository.signInWithApple(credential) } returns Result.failure(error)

            val result = useCase(credential)

            assertEquals(error, result.exceptionOrNull())
            coVerify(exactly = 0) { authRepository.saveUserToFirestore(any()) }
        }
}
