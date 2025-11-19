package com.jesuslcorominas.teamflowmanager.ui.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.entity.MatchPeriodEntity
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseExporter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class DatabaseExporterImpl(
    private val context: Context,
    private val database: TeamFlowManagerDatabase
) : DatabaseExporter {

    override suspend fun exportDatabase(): String? {
        return try {
            val fileName = "teamflowmanager_backup_${System.currentTimeMillis()}.tfm"
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    // Export Team table
                    val teams = database.teamDao().getTeamDirect()
                    teams?.let { team ->
                        writer.write(
                            "INSERT INTO team (id, name, coachName, delegateName, captainId) VALUES " +
                                    "(${team.id}, '${escapeSql(team.name)}', '${escapeSql(team.coachName)}', " +
                                    "'${escapeSql(team.delegateName)}', ${team.captainId});\n"
                        )
                    }

                    // Export Players table
                    val players = database.playerDao().getAllPlayersDirect()
                    players.forEach { player ->
                        writer.write(
                            "INSERT INTO players (id, firstName, lastName, number, positions, teamId, isCaptain, imageUri) VALUES " +
                                    "(${player.id}, '${escapeSql(player.firstName)}', '${escapeSql(player.lastName)}', " +
                                    "${player.number}, '${escapeSql(player.positions)}', ${player.teamId}, " +
                                    "${if (player.isCaptain) 1 else 0}, ${if (player.imageUri != null) "'${escapeSql(player.imageUri)}'" else "NULL"});\n"
                        )
                    }

                    // Export Match table
                    val matches = database.matchDao().getAllMatchesDirect()
                    matches.forEach { match ->
                        writer.write(
                            "INSERT INTO match (id, teamId, teamName, opponent, location, dateTime, numberOfPeriods, squadCallUpIds, captainId, startingLineupIds, elapsedTimeMillis, lastStartTimeMillis, status, archived, currentPeriod, pauseCount, goals, opponentGoals, timeoutStartTimeMillis, periods, periodType) VALUES " +
                                    "(${match.id}, ${match.teamId}, '${escapeSql(match.teamName)}', '${escapeSql(match.opponent)}', " +
                                    "'${escapeSql(match.location)}', ${match.dateTime}, ${match.numberOfPeriods}, " +
                                    "'${escapeSql(match.squadCallUpIds)}', ${match.captainId}, '${escapeSql(match.startingLineupIds)}', " +
                                    "${match.elapsedTimeMillis}, ${match.lastStartTimeMillis}, '${escapeSql(match.status)}', " +
                                    "${if (match.archived) 1 else 0}, ${match.currentPeriod}, ${match.pauseCount}, " +
                                    "${match.goals}, ${match.opponentGoals}, ${match.timeoutStartTimeMillis}, " +
                                    "'${escapeSql(serializePeriods(match.periods))}', ${match.periodType});\n"
                        )
                    }

                    // Export PlayerTime table
                    val playerTimes = database.playerTimeDao().getAllPlayerTimesDirect()
                    playerTimes.forEach { playerTime ->
                        writer.write(
                            "INSERT INTO player_time (playerId, elapsedTimeMillis, isRunning, lastStartTimeMillis, status) VALUES " +
                                    "(${playerTime.playerId}, ${playerTime.elapsedTimeMillis}, " +
                                    "${if (playerTime.isRunning) 1 else 0}, ${playerTime.lastStartTimeMillis}, " +
                                    "'${escapeSql(playerTime.status)}');\n"
                        )
                    }

                    // Export PlayerTimeHistory table
                    val playerTimeHistory = database.playerTimeHistoryDao().getAllPlayerTimeHistoryDirect()
                    playerTimeHistory.forEach { history ->
                        writer.write(
                            "INSERT INTO player_time_history (id, playerId, matchId, elapsedTimeMillis, savedAtMillis) VALUES " +
                                    "(${history.id}, ${history.playerId}, ${history.matchId}, " +
                                    "${history.elapsedTimeMillis}, ${history.savedAtMillis});\n"
                        )
                    }

                    // Export PlayerSubstitution table
                    val playerSubstitutions = database.playerSubstitutionDao().getAllPlayerSubstitutionsDirect()
                    playerSubstitutions.forEach { substitution ->
                        writer.write(
                            "INSERT INTO player_substitution (id, matchId, playerOutId, playerInId, substitutionTimeMillis, matchElapsedTimeMillis) VALUES " +
                                    "(${substitution.id}, ${substitution.matchId}, ${substitution.playerOutId}, " +
                                    "${substitution.playerInId}, ${substitution.substitutionTimeMillis}, ${substitution.matchElapsedTimeMillis});\n"
                        )
                    }

                    // Export Goal table
                    val goals = database.goalDao().getAllGoalsDirect()
                    goals.forEach { goal ->
                        writer.write(
                            "INSERT INTO goal (id, matchId, scorerId, goalTimeMillis, matchElapsedTimeMillis, isOpponentGoal) VALUES " +
                                    "(${goal.id}, ${goal.matchId}, ${goal.scorerId}, ${goal.goalTimeMillis}, " +
                                    "${goal.matchElapsedTimeMillis}, ${if (goal.isOpponentGoal) 1 else 0});\n"
                        )
                    }
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            uri.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeSql(value: String): String {
        return value.replace("'", "''")
    }

    private fun serializePeriods(periods: List<MatchPeriodEntity>): String {
        // Simple JSON-like serialization matching Room's type converter
        return periods.joinToString(",") { period ->
            "{\"periodNumber\":${period.periodNumber},\"periodDuration\":${period.periodDuration}," +
                    "\"startTimeMillis\":${period.startTimeMillis},\"endTimeMillis\":${period.endTimeMillis}}"
        }
    }
}
