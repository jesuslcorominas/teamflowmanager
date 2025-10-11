package com.jesuslcorominas.teamflowmanager.data.local.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.dao.MatchDao
import com.jesuslcorominas.teamflowmanager.data.local.entity.toDomain
import com.jesuslcorominas.teamflowmanager.data.local.entity.toEntity
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class MatchLocalDataSourceImpl(
    private val matchDao: MatchDao,
) : MatchLocalDataSource {
    override fun getMatch(): Flow<Match?> = matchDao.getMatch().map { it?.toDomain() }

    override suspend fun upsertMatch(match: Match) {
        matchDao.upsertMatch(match.toEntity())
    }
}
