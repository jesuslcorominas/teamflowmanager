package com.jesuslcorominas.teamflowmanager.data.local.sqldelight

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.entity.MatchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class MatchDaoWrapper(
    private val database: TeamFlowManagerDatabase
) {
    fun getMatchById(matchId: Long): Flow<MatchEntity?> =
        database.matchQueries
            .getMatchById(matchId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { match ->
                match?.let { toEntity(it) }
            }

    fun getAllMatches(): Flow<List<MatchEntity>> =
        database.matchQueries
            .getAllMatches()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { matches ->
                matches.map { toEntity(it) }
            }

    fun getArchivedMatches(): Flow<List<MatchEntity>> =
        database.matchQueries
            .getArchivedMatches()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { matches ->
                matches.map { toEntity(it) }
            }

    suspend fun upsertMatch(match: MatchEntity) {
        val existing = database.matchQueries.getMatchById(match.id).executeAsOneOrNull()
        if (existing != null) {
            updateMatch(match)
        } else {
            insertMatchInternal(match)
        }
    }

    suspend fun insertMatch(match: MatchEntity): Long {
        database.matchQueries.insertMatch(
            teamId = match.teamId,
            teamName = match.teamName,
            opponent = match.opponent,
            location = match.location,
            dateTime = match.dateTime,
            numberOfPeriods = match.numberOfPeriods.toLong(),
            squadCallUpIds = match.squadCallUpIds,
            captainId = match.captainId,
            startingLineupIds = match.startingLineupIds,
            elapsedTimeMillis = match.elapsedTimeMillis,
            lastStartTimeMillis = match.lastStartTimeMillis,
            status = match.status,
            archived = if (match.archived) 1L else 0L,
            currentPeriod = match.currentPeriod.toLong(),
            pauseCount = match.pauseCount.toLong(),
            goals = match.goals.toLong(),
            opponentGoals = match.opponentGoals.toLong(),
            timeoutStartTimeMillis = match.timeoutStartTimeMillis,
            periods = match.periods,
            periodType = match.periodType.toLong()
        )
        return database.matchQueries.lastInsertRowId().executeAsOne()
    }

    private suspend fun insertMatchInternal(match: MatchEntity) {
        database.matchQueries.insertMatch(
            teamId = match.teamId,
            teamName = match.teamName,
            opponent = match.opponent,
            location = match.location,
            dateTime = match.dateTime,
            numberOfPeriods = match.numberOfPeriods.toLong(),
            squadCallUpIds = match.squadCallUpIds,
            captainId = match.captainId,
            startingLineupIds = match.startingLineupIds,
            elapsedTimeMillis = match.elapsedTimeMillis,
            lastStartTimeMillis = match.lastStartTimeMillis,
            status = match.status,
            archived = if (match.archived) 1L else 0L,
            currentPeriod = match.currentPeriod.toLong(),
            pauseCount = match.pauseCount.toLong(),
            goals = match.goals.toLong(),
            opponentGoals = match.opponentGoals.toLong(),
            timeoutStartTimeMillis = match.timeoutStartTimeMillis,
            periods = match.periods,
            periodType = match.periodType.toLong()
        )
    }

    suspend fun updateMatch(match: MatchEntity) {
        database.matchQueries.updateMatch(
            teamId = match.teamId,
            teamName = match.teamName,
            opponent = match.opponent,
            location = match.location,
            dateTime = match.dateTime,
            numberOfPeriods = match.numberOfPeriods.toLong(),
            squadCallUpIds = match.squadCallUpIds,
            captainId = match.captainId,
            startingLineupIds = match.startingLineupIds,
            elapsedTimeMillis = match.elapsedTimeMillis,
            lastStartTimeMillis = match.lastStartTimeMillis,
            status = match.status,
            archived = if (match.archived) 1L else 0L,
            currentPeriod = match.currentPeriod.toLong(),
            pauseCount = match.pauseCount.toLong(),
            goals = match.goals.toLong(),
            opponentGoals = match.opponentGoals.toLong(),
            timeoutStartTimeMillis = match.timeoutStartTimeMillis,
            periods = match.periods,
            periodType = match.periodType.toLong(),
            id = match.id
        )
    }

    suspend fun deleteMatch(matchId: Long) {
        database.matchQueries.deleteMatch(matchId)
    }

    suspend fun getScheduledMatches(): List<MatchEntity> {
        return database.matchQueries.getScheduledMatches()
            .executeAsList()
            .map { toEntity(it) }
    }

    suspend fun updateMatchCaptain(matchId: Long, captainId: Long?) {
        database.matchQueries.updateMatchCaptain(
            captainId = captainId,
            id = matchId
        )
    }

    private fun toEntity(match: com.jesuslcorominas.teamflowmanager.data.local.database.Match): MatchEntity {
        return MatchEntity(
            id = match.id,
            teamId = match.teamId,
            teamName = match.teamName,
            opponent = match.opponent,
            location = match.location,
            dateTime = match.dateTime,
            numberOfPeriods = match.numberOfPeriods.toInt(),
            squadCallUpIds = match.squadCallUpIds,
            captainId = match.captainId,
            startingLineupIds = match.startingLineupIds,
            elapsedTimeMillis = match.elapsedTimeMillis,
            lastStartTimeMillis = match.lastStartTimeMillis,
            status = match.status,
            archived = match.archived,
            currentPeriod = match.currentPeriod.toInt(),
            pauseCount = match.pauseCount.toInt(),
            goals = match.goals.toInt(),
            opponentGoals = match.opponentGoals.toInt(),
            timeoutStartTimeMillis = match.timeoutStartTimeMillis,
            periods = match.periods,
            periodType = match.periodType.toInt()
        )
    }
}
