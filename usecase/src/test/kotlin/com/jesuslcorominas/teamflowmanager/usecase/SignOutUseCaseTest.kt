package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SignOutUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var signOutUseCase: SignOutUseCase

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        signOutUseCase = SignOutUseCaseImpl(authRepository)
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
}
