package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import kotlinx.coroutines.flow.Flow

interface PlayerSubstitutionLocalDataSource {
    fun getMatchSubstitutions(matchId: Long): Flow<List<PlayerSubstitution>>

    suspend fun insertSubstitution(substitution: PlayerSubstitution): Long
}
