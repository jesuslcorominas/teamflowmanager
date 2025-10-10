package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface TeamLocalDataSource {
    fun getTeam(): Flow<Team?>

    suspend fun insertTeam(team: Team)

    suspend fun updateTeam(team: Team)
}
