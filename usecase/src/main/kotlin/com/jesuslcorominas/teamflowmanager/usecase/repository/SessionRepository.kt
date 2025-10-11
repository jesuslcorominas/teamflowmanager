package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getSession(): Flow<Session?>

    suspend fun startTimer(currentTimeMillis: Long)

    suspend fun pauseTimer(currentTimeMillis: Long)
}
