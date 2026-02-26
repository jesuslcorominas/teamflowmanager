package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class MatchRepositoryImpl(
    private val matchDataSource: MatchDataSource
) : MatchRepository {
    override fun getMatchById(matchId: Long): Flow<Match?> = matchDataSource.getMatchById(matchId)

    override fun getAllMatches(): Flow<List<Match>> = matchDataSource.getAllMatches()

    override fun getArchivedMatches(): Flow<List<Match>> = matchDataSource.getArchivedMatches()

    override suspend fun getScheduledMatches(): List<Match> = matchDataSource.getScheduledMatches()

    override suspend fun updateMatchCaptain(matchId: Long, captainId: Long?) {
        matchDataSource.updateMatchCaptain(matchId, captainId)
    }

    override suspend fun createMatch(match: Match): Long = matchDataSource.insertMatch(match)

    override suspend fun updateMatch(match: Match) {
        matchDataSource.updateMatch(match)
    }

    override suspend fun deleteMatch(matchId: Long) {
        matchDataSource.deleteMatch(matchId)
    }

    override suspend fun startTimer(matchId: Long, currentTimeMillis: Long) {
        matchDataSource.getMatchById(matchId).first()?.let { currentMatch ->
            val firstNotStartedPeriod = currentMatch.periods.first { it.startTimeMillis == 0L }

            val updatedMatch = currentMatch.copy(
                status = MatchStatus.IN_PROGRESS,
                periods = currentMatch.periods.map { period ->
                    if (period.periodNumber == firstNotStartedPeriod.periodNumber) {
                        period.copy(startTimeMillis = currentTimeMillis)
                    } else {
                        period
                    }
                },
            )

            matchDataSource.updateMatch(updatedMatch)
        }
    }

    override suspend fun pauseTimer(matchId: Long, currentTimeMillis: Long) {
        val currentMatch = matchDataSource.getMatchById(matchId).first()
        if (currentMatch != null && currentMatch.status == MatchStatus.IN_PROGRESS) {
            val firstNotFinishedPeriod = currentMatch.periods.first { it.endTimeMillis == 0L }

            val updatedMatch =
                currentMatch.copy(
                    periods = currentMatch.periods.map { period ->
                        if (period.periodNumber == firstNotFinishedPeriod.periodNumber) {
                            period.copy(endTimeMillis = currentTimeMillis)
                        } else {
                            period
                        }
                    },
                    status = MatchStatus.PAUSED,
                    pauseCount = currentMatch.pauseCount + 1,
                )

            matchDataSource.updateMatch(updatedMatch)
        }
    }

    override suspend fun startTimeout(matchId: Long, currentTimeMillis: Long) {
        val currentMatch = matchDataSource.getMatchById(matchId).first()
        if (currentMatch != null && currentMatch.status == MatchStatus.IN_PROGRESS) {
            val updatedMatch = currentMatch.copy(
                status = MatchStatus.TIMEOUT,
                timeoutStartTimeMillis = currentTimeMillis
            )
            matchDataSource.updateMatch(updatedMatch)
        }
    }

    override suspend fun endTimeout(matchId: Long, currentTimeMillis: Long) {
        val currentMatch = matchDataSource.getMatchById(matchId).first()
        if (currentMatch != null && currentMatch.status == MatchStatus.TIMEOUT) {
            val timeoutStartTime = currentMatch.timeoutStartTimeMillis
            val timeoutDuration = currentTimeMillis - timeoutStartTime

            // Adjust the current period's start time to account for the timeout
            val currentPeriod = currentMatch.periods.firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }

            val updatedMatch = if (currentPeriod != null) {
                currentMatch.copy(
                    status = MatchStatus.IN_PROGRESS,
                    timeoutStartTimeMillis = 0L,
                    periods = currentMatch.periods.map { period ->
                        if (period.periodNumber == currentPeriod.periodNumber) {
                            period.copy(startTimeMillis = period.startTimeMillis + timeoutDuration)
                        } else {
                            period
                        }
                    }
                )
            } else {
                currentMatch.copy(
                    status = MatchStatus.IN_PROGRESS,
                    timeoutStartTimeMillis = 0L
                )
            }

            matchDataSource.updateMatch(updatedMatch)
        }
    }

    override suspend fun archiveMatch(matchId: Long) {
        val match = matchDataSource.getMatchById(matchId).first()
        if (match != null) {
            matchDataSource.updateMatch(match.copy(archived = true))
        }
    }

    override suspend fun unarchiveMatch(matchId: Long) {
        val match = matchDataSource.getMatchById(matchId).first()
        if (match != null) {
            matchDataSource.updateMatch(match.copy(archived = false))
        }
    }

    override suspend fun updateMatchWithOperationId(match: Match, operationId: String) {
        val updatedMatch = match.copy(lastCompletedOperationId = operationId)
        matchDataSource.updateMatch(updatedMatch)
    }
}
