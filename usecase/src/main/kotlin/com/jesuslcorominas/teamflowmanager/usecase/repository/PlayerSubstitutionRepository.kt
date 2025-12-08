package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import kotlinx.coroutines.flow.Flow

interface PlayerSubstitutionRepository {
    fun getMatchSubstitutions(matchId: Long): Flow<List<PlayerSubstitution>>

    suspend fun insertSubstitution(substitution: PlayerSubstitution): Long

    /**
     * Get all local substitutions directly (not as a Flow) for migration purposes.
     * @return List of all substitutions
     */
    suspend fun getAllLocalPlayerSubstitutionsDirect(): List<PlayerSubstitution>

    /**
     * Clear local substitution data from Room database.
     * Used after successful migration to Firestore.
     */
    suspend fun clearLocalPlayerSubstitutionData()
}
