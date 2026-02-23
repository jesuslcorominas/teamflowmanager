package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetCurrentUserUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        getCurrentUserUseCase = GetCurrentUserUseCaseImpl(authRepository)
    }

    @Test
    fun `invoke should return user from repository when authenticated`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://example.com/photo.jpg"
        )
        every { authRepository.getCurrentUser() } returns flowOf(user)

        // When
        val result = getCurrentUserUseCase.invoke().first()

        // Then
        assertEquals(user, result)
        verify { authRepository.getCurrentUser() }
    }

    @Test
    fun `invoke should return null when not authenticated`() = runTest {
        // Given
        every { authRepository.getCurrentUser() } returns flowOf(null)

        // When
        val result = getCurrentUserUseCase.invoke().first()

        // Then
        assertNull(result)
        verify { authRepository.getCurrentUser() }
    }
}
