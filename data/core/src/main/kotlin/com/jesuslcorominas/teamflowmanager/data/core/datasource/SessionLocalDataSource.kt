package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionLocalDataSource {
    fun getSession(): Flow<Session?>

    suspend fun upsertSession(session: Session)
}
