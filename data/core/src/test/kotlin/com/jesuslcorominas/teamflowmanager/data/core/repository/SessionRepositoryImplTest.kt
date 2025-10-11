package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.SessionLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Session
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionRepositoryImplTest {
    private lateinit var localDataSource: SessionLocalDataSource
    private lateinit var repository: SessionRepositoryImpl

    @Before
    fun setup() {
        localDataSource = mockk(relaxed = true)
        repository = SessionRepositoryImpl(localDataSource)
    }

    @Test
    fun `getSession should return session from local data source`() =
        runTest {
            // Given
            val session = Session(id = 1L, elapsedTimeMillis = 5000L, isRunning = true)
            every { localDataSource.getSession() } returns flowOf(session)

            // When
            val result = repository.getSession().first()

            // Then
            assertEquals(session, result)
        }

    @Test
    fun `startTimer should create new session when none exists`() =
        runTest {
            // Given
            val currentTime = 1000L
            every { localDataSource.getSession() } returns flowOf(null)
            coEvery { localDataSource.upsertSession(any()) } returns Unit

            // When
            repository.startTimer(currentTime)

            // Then
            coVerify {
                localDataSource.upsertSession(
                    match {
                        it.id == 1L &&
                            it.elapsedTimeMillis == 0L &&
                            it.isRunning &&
                            it.lastStartTimeMillis == currentTime
                    },
                )
            }
        }

    @Test
    fun `startTimer should update existing session to running state`() =
        runTest {
            // Given
            val currentTime = 2000L
            val existingSession = Session(id = 1L, elapsedTimeMillis = 5000L, isRunning = false)
            every { localDataSource.getSession() } returns flowOf(existingSession)
            coEvery { localDataSource.upsertSession(any()) } returns Unit

            // When
            repository.startTimer(currentTime)

            // Then
            coVerify {
                localDataSource.upsertSession(
                    match {
                        it.id == 1L &&
                            it.elapsedTimeMillis == 5000L &&
                            it.isRunning &&
                            it.lastStartTimeMillis == currentTime
                    },
                )
            }
        }

    @Test
    fun `pauseTimer should update elapsed time and set running to false`() =
        runTest {
            // Given
            val startTime = 1000L
            val pauseTime = 3000L
            val existingSession = Session(id = 1L, elapsedTimeMillis = 2000L, isRunning = true, lastStartTimeMillis = startTime)
            every { localDataSource.getSession() } returns flowOf(existingSession)
            coEvery { localDataSource.upsertSession(any()) } returns Unit

            // When
            repository.pauseTimer(pauseTime)

            // Then
            coVerify {
                localDataSource.upsertSession(
                    match {
                        it.id == 1L &&
                            it.elapsedTimeMillis == 4000L &&
                            !it.isRunning &&
                            it.lastStartTimeMillis == null
                    },
                )
            }
        }

    @Test
    fun `pauseTimer should do nothing when session is not running`() =
        runTest {
            // Given
            val pauseTime = 3000L
            val existingSession = Session(id = 1L, elapsedTimeMillis = 2000L, isRunning = false)
            every { localDataSource.getSession() } returns flowOf(existingSession)

            // When
            repository.pauseTimer(pauseTime)

            // Then
            coVerify(exactly = 0) { localDataSource.upsertSession(any()) }
        }

    @Test
    fun `pauseTimer should do nothing when no session exists`() =
        runTest {
            // Given
            val pauseTime = 3000L
            every { localDataSource.getSession() } returns flowOf(null)

            // When
            repository.pauseTimer(pauseTime)

            // Then
            coVerify(exactly = 0) { localDataSource.upsertSession(any()) }
        }
}
