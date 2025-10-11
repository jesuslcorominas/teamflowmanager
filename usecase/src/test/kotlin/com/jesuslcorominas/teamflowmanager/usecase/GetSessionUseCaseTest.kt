package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Session
import com.jesuslcorominas.teamflowmanager.usecase.repository.SessionRepository
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

class GetSessionUseCaseTest {
    private lateinit var sessionRepository: SessionRepository
    private lateinit var getSessionUseCase: GetSessionUseCase

    @Before
    fun setup() {
        sessionRepository = mockk(relaxed = true)
        getSessionUseCase = GetSessionUseCaseImpl(sessionRepository)
    }

    @Test
    fun `invoke should return session from repository`() =
        runTest {
            // Given
            val session = Session(id = 1L, elapsedTimeMillis = 5000L, isRunning = true)
            every { sessionRepository.getSession() } returns flowOf(session)

            // When
            val result = getSessionUseCase.invoke().first()

            // Then
            assertEquals(session, result)
            verify { sessionRepository.getSession() }
        }

    @Test
    fun `invoke should return null when no session exists`() =
        runTest {
            // Given
            every { sessionRepository.getSession() } returns flowOf(null)

            // When
            val result = getSessionUseCase.invoke().first()

            // Then
            assertNull(result)
            verify { sessionRepository.getSession() }
        }
}
