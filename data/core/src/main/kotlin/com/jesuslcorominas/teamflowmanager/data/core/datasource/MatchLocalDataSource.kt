package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchLocalDataSource {
    fun getMatch(): Flow<Match?>

    suspend fun upsertMatch(match: Match)
}
