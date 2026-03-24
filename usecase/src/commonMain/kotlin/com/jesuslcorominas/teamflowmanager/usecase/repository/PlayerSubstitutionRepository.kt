package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import kotlinx.coroutines.flow.Flow

interface PlayerSubstitutionRepository {
    fun getMatchSubstitutions(matchId: Long): Flow<List<PlayerSubstitution>>

    suspend fun insertSubstitution(substitution: PlayerSubstitution): Long
}
