package com.jesuslcorominas.teamflowmanager.data.local.sqldelight

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerSubstitutionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PlayerSubstitutionDaoWrapper(
    private val database: TeamFlowManagerDatabase
) {
    fun getMatchSubstitutions(matchId: Long): Flow<List<PlayerSubstitutionEntity>> =
        database.playerSubstitutionQueries
            .getPlayerSubstitutionsByMatchId(matchId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { substitutions ->
                substitutions.map {
                    PlayerSubstitutionEntity(
                        id = it.id,
                        matchId = it.matchId,
                        playerOutId = it.playerOutId,
                        playerInId = it.playerInId,
                        substitutionTimeMillis = it.substitutionTimeMillis,
                        matchElapsedTimeMillis = it.matchElapsedTimeMillis
                    )
                }
            }

    suspend fun insert(substitution: PlayerSubstitutionEntity): Long {
        database.playerSubstitutionQueries.insertPlayerSubstitution(
            matchId = substitution.matchId,
            playerOutId = substitution.playerOutId,
            playerInId = substitution.playerInId,
            substitutionTimeMillis = substitution.substitutionTimeMillis,
            matchElapsedTimeMillis = substitution.matchElapsedTimeMillis
        )
        return database.playerSubstitutionQueries.lastInsertRowId().executeAsOne()
    }
}
