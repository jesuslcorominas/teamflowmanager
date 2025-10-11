package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getMatch(): Flow<Match?>

    suspend fun startTimer(currentTimeMillis: Long)

    suspend fun pauseTimer(currentTimeMillis: Long)
}
