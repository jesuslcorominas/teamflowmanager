package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getAllPlayers(): Flow<List<Player>>
    suspend fun addPlayer(player: Player)
}
