package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.SessionLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Session
import com.jesuslcorominas.teamflowmanager.usecase.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class SessionRepositoryImpl(
    private val localDataSource: SessionLocalDataSource,
) : SessionRepository {
    override fun getSession(): Flow<Session?> = localDataSource.getSession()

    override suspend fun startTimer(currentTimeMillis: Long) {
        val currentSession = localDataSource.getSession().first()
        val session =
            if (currentSession != null) {
                currentSession.copy(
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                )
            } else {
                Session(
                    id = 1L,
                    elapsedTimeMillis = 0L,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                )
            }
        localDataSource.upsertSession(session)
    }

    override suspend fun pauseTimer(currentTimeMillis: Long) {
        val currentSession = localDataSource.getSession().first()
        if (currentSession != null && currentSession.isRunning) {
            val lastStartTime = currentSession.lastStartTimeMillis ?: currentTimeMillis
            val additionalTime = currentTimeMillis - lastStartTime
            val updatedSession =
                currentSession.copy(
                    elapsedTimeMillis = currentSession.elapsedTimeMillis + additionalTime,
                    isRunning = false,
                    lastStartTimeMillis = null,
                )
            localDataSource.upsertSession(updatedSession)
        }
    }
}
