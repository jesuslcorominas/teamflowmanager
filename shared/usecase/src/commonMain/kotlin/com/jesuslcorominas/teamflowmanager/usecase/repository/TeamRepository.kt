package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun getTeam(): Flow<Team?>

    suspend fun createTeam(team: Team)

    suspend fun updateTeam(team: Team)
}
