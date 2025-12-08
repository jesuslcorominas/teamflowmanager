package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import kotlinx.coroutines.flow.Flow

internal class PlayerSubstitutionRepositoryImpl(
    private val playerSubstitutionDataSource: PlayerSubstitutionDataSource,
    private val playerSubstitutionLocalDataSource: PlayerSubstitutionDataSource,
) : PlayerSubstitutionRepository {
    override fun getMatchSubstitutions(matchId: Long): Flow<List<PlayerSubstitution>> =
        playerSubstitutionDataSource.getMatchSubstitutions(matchId)

    override suspend fun insertSubstitution(substitution: PlayerSubstitution): Long =
        playerSubstitutionDataSource.insertSubstitution(substitution)

    override suspend fun getAllLocalPlayerSubstitutionsDirect(): List<PlayerSubstitution> =
        playerSubstitutionLocalDataSource.getAllPlayerSubstitutionsDirect()

    override suspend fun clearLocalPlayerSubstitutionData() {
        playerSubstitutionLocalDataSource.clearLocalData()
    }
}
