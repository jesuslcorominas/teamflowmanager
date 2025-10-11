package com.jesuslcorominas.teamflowmanager.data.local.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.SessionLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.dao.SessionDao
import com.jesuslcorominas.teamflowmanager.data.local.entity.toDomain
import com.jesuslcorominas.teamflowmanager.data.local.entity.toEntity
import com.jesuslcorominas.teamflowmanager.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class SessionLocalDataSourceImpl(
    private val sessionDao: SessionDao,
) : SessionLocalDataSource {
    override fun getSession(): Flow<Session?> = sessionDao.getSession().map { it?.toDomain() }

    override suspend fun upsertSession(session: Session) {
        sessionDao.upsertSession(session.toEntity())
    }
}
