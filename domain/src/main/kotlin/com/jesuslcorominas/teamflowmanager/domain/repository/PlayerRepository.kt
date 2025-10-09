package com.jesuslcorominas.teamflowmanager.domain.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for player data operations
 */
interface PlayerRepository {
    /**
     * Get all players as a Flow
     */
    fun getAllPlayers(): Flow<List<Player>>
}
