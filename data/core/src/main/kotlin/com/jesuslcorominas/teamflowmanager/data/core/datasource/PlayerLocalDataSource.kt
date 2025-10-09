package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.flow.Flow

/**
 * Local data source interface for player data
 */
interface PlayerLocalDataSource {
    fun getAllPlayers(): Flow<List<Player>>
}
