package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignOutUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var signOutUseCase: SignOutUseCase

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
        signOutUseCase = SignOutUseCaseImpl(authRepository, preferencesRepository)
    }

    @Test
    fun `invoke should call repository signOut`() = runTest {
        // Given
        coEvery { authRepository.signOut() } just runs

        // When
        signOutUseCase.invoke()

        // Then
        coVerify { authRepository.signOut() }
    }

    @Test
    fun `invoke should clear account preferences after signing out`() = runTest {
        // When
        signOutUseCase.invoke()

        // Then
        coVerifyOrder {
            authRepository.signOut()
            preferencesRepository.clearAccountData()
        }
    }

    @Test
    fun `invoke should keep account preferences when sign out fails`() = runTest {
        // Given
        coEvery { authRepository.signOut() } throws IllegalStateException("network")

        // When
        val result = runCatching { signOutUseCase.invoke() }

        // Then
        assertTrue(result.isFailure)
        verify(exactly = 0) { preferencesRepository.clearAccountData() }
    }
}
