package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class MatchRepositoryImpl(
    private val localDataSource: MatchLocalDataSource,
) : MatchRepository {
    override fun getMatch(): Flow<Match?> = localDataSource.getMatch()

    override fun getMatchById(matchId: Long): Flow<Match?> = localDataSource.getMatchById(matchId)

    override fun getAllMatches(): Flow<List<Match>> = localDataSource.getAllMatches()

    override fun getArchivedMatches(): Flow<List<Match>> = localDataSource.getArchivedMatches()

    override suspend fun getScheduledMatches(): List<Match> = localDataSource.getScheduledMatches()

    override suspend fun updateMatchCaptain(matchId: Long, captainId: Long?) {
        localDataSource.updateMatchCaptain(matchId, captainId)
    }

    override suspend fun createMatch(match: Match): Long = localDataSource.insertMatch(match)

    override suspend fun updateMatch(match: Match) {
        localDataSource.updateMatch(match)
    }

    override suspend fun deleteMatch(matchId: Long) {
        localDataSource.deleteMatch(matchId)
    }

    override suspend fun startTimer(currentTimeMillis: Long) {
        val currentMatch = localDataSource.getMatch().first()
        val match =
            if (currentMatch != null) {
                currentMatch.copy(
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                )
            } else {
                Match(
                    id = 1L,
                    elapsedTimeMillis = 0L,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                )
            }
        localDataSource.upsertMatch(match)
    }

    override suspend fun pauseTimer(currentTimeMillis: Long) {
        val currentMatch = localDataSource.getMatch().first()
        if (currentMatch != null && currentMatch.isRunning) {
            val lastStartTime = currentMatch.lastStartTimeMillis ?: currentTimeMillis
            val additionalTime = currentTimeMillis - lastStartTime
            val updatedMatch =
                currentMatch.copy(
                    elapsedTimeMillis = currentMatch.elapsedTimeMillis + additionalTime,
                    isRunning = false,
                    lastStartTimeMillis = null,
                    pauseCount = currentMatch.pauseCount + 1,
                    currentPeriod = minOf(currentMatch.currentPeriod + 1, currentMatch.numberOfPeriods),
                )
            localDataSource.upsertMatch(updatedMatch)
        }
    }

    override suspend fun archiveMatch(matchId: Long) {
        val match = localDataSource.getMatchById(matchId).first()
        if (match != null) {
            localDataSource.updateMatch(match.copy(archived = true))
        }
    }

    override suspend fun unarchiveMatch(matchId: Long) {
        val match = localDataSource.getMatchById(matchId).first()
        if (match != null) {
            localDataSource.updateMatch(match.copy(archived = false))
        }
    }
}
