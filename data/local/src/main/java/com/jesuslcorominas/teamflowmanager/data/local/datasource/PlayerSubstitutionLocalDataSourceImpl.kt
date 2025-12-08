package com.jesuslcorominas.teamflowmanager.data.local.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerSubstitutionDao
import com.jesuslcorominas.teamflowmanager.data.local.entity.toEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PlayerSubstitutionLocalDataSourceImpl(
    private val playerSubstitutionDao: PlayerSubstitutionDao,
) : PlayerSubstitutionDataSource {
    override fun getMatchSubstitutions(matchId: Long): Flow<List<PlayerSubstitution>> =
        playerSubstitutionDao.getMatchSubstitutions(matchId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insertSubstitution(substitution: PlayerSubstitution): Long =
        playerSubstitutionDao.insert(substitution.toEntity())

    override suspend fun getAllPlayerSubstitutionsDirect(): List<PlayerSubstitution> =
        playerSubstitutionDao.getAllPlayerSubstitutionsDirect().map { it.toDomain() }

    override suspend fun clearLocalData() {
        playerSubstitutionDao.deleteAllPlayerSubstitutions()
    }
}
