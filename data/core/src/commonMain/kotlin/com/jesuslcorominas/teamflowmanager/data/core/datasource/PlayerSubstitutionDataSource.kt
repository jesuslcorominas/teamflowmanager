package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import kotlinx.coroutines.flow.Flow

interface PlayerSubstitutionDataSource {
    fun getMatchSubstitutions(matchId: String): Flow<List<PlayerSubstitution>>

    suspend fun insertSubstitution(substitution: PlayerSubstitution): String

    /**
     * Get all substitutions directly (not as a Flow) for migration purposes.
     * @return List of all substitutions
     */
    suspend fun getAllPlayerSubstitutionsDirect(): List<PlayerSubstitution>

    /**
     * Clear all substitution data from local storage.
     * Only applicable for local data sources.
     */
    suspend fun clearLocalData()
}
