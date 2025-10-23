package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class MatchRepositoryImpl(
    private val localDataSource: MatchLocalDataSource,
) : MatchRepository {
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

    override suspend fun startTimer(matchId: Long, currentTimeMillis: Long) {
        localDataSource.getMatchById(matchId).first()?.let { currentMatch ->
            val updatedMatch = currentMatch.copy(
                status = MatchStatus.IN_PROGRESS,
                lastStartTimeMillis = currentTimeMillis,
            )

            localDataSource.upsertMatch(updatedMatch)
        }
    }

    override suspend fun pauseTimer(matchId: Long, currentTimeMillis: Long) {
        val currentMatch = localDataSource.getMatchById(matchId).first()
        if (currentMatch != null && currentMatch.status == MatchStatus.IN_PROGRESS) {
            val lastStartTime = currentMatch.lastStartTimeMillis ?: currentTimeMillis
            val additionalTime = currentTimeMillis - lastStartTime
            val updatedMatch =
                currentMatch.copy(
                    elapsedTimeMillis = currentMatch.elapsedTimeMillis + additionalTime,
                    status = MatchStatus.PAUSED,
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
