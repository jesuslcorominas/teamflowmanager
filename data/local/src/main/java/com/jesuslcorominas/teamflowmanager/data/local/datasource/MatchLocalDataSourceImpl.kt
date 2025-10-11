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

    override fun getMatchById(matchId: Long): Flow<Match?> =
        matchDao.getMatchById(matchId).map { it?.toDomain() }

    override fun getAllMatches(): Flow<List<Match>> =
        matchDao.getAllMatches().map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertMatch(match: Match) {
        matchDao.upsertMatch(match.toEntity())
    }

    override suspend fun insertMatch(match: Match): Long = matchDao.insertMatch(match.toEntity())

    override suspend fun updateMatch(match: Match) {
        matchDao.updateMatch(match.toEntity())
    }

    override suspend fun deleteMatch(matchId: Long) {
        matchDao.deleteMatch(matchId)
    }
}
