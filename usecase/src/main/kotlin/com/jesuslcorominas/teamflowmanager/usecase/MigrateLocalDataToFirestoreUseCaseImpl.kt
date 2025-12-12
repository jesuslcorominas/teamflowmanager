package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MigrationStep
import com.jesuslcorominas.teamflowmanager.domain.usecase.MigrateLocalDataToFirestoreUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository

internal class MigrateLocalDataToFirestoreUseCaseImpl(
    private val teamRepository: TeamRepository,
    private val playerRepository: PlayerRepository,
    private val matchRepository: MatchRepository,
    private val goalRepository: GoalRepository,
    private val playerSubstitutionRepository: PlayerSubstitutionRepository,
    private val playerTimeRepository: PlayerTimeRepository,
    private val playerTimeHistoryRepository: PlayerTimeHistoryRepository,
) : MigrateLocalDataToFirestoreUseCase {

    companion object {
        private const val TAG = "MigrateLocalDataUseCase"
        private const val TOTAL_STEPS = 8
    }

    override suspend fun invoke(
        userId: String,
        onProgress: (MigrationStep) -> Unit
    ): Result<Unit> {
        return try {
            println("$TAG: Starting local data migration to Firestore for user: $userId")

            // Step 1: Migrate Team
            onProgress(MigrationStep(1, TOTAL_STEPS, "Migrando equipo..."))
            val team = teamRepository.getLocalTeamDirect()
            if (team == null) {
                println("$TAG: No local team found to migrate")
                return Result.failure(IllegalStateException("No local team found"))
            }

            println("$TAG: Found local team: ${team.name}, migrating...")
            // Create team in Firestore with userId as coachId
            val teamWithCoachId = team.copy(coachId = userId)
            teamRepository.createTeam(teamWithCoachId)
            println("$TAG: Team migrated successfully")

            // Step 2: Migrate Players and build ID mapping
            onProgress(MigrationStep(2, TOTAL_STEPS, "Migrando jugadores..."))
            val players = playerRepository.getAllLocalPlayersDirect()
            println("$TAG: Found ${players.size} players to migrate")
            val playerIdMap = mutableMapOf<Long, Long>() // old ID -> new ID

            players.forEach { player ->
                val oldId = player.id
                val newId = playerRepository.addPlayer(player)
                playerIdMap[oldId] = newId
                println("$TAG: Player ID mapping: $oldId -> $newId")
            }
            println("$TAG: All players migrated successfully with ${playerIdMap.size} ID mappings")

            // Step 3: Migrate Matches and build ID mapping, updating player references
            onProgress(MigrationStep(3, TOTAL_STEPS, "Migrando partidos..."))
            val matches = matchRepository.getAllLocalMatchesDirect()
            println("$TAG: Found ${matches.size} matches to migrate")
            val matchIdMap = mutableMapOf<Long, Long>() // old ID -> new ID

            matches.forEach { match ->
                val oldMatchId = match.id

                // Update player references in match
                val updatedMatch = match.copy(
                    captainId = playerIdMap[match.captainId] ?: match.captainId,
                    squadCallUpIds = match.squadCallUpIds.map { playerIdMap[it] ?: it },
                    startingLineupIds = match.startingLineupIds.map { playerIdMap[it] ?: it }
                )

                val newMatchId = matchRepository.createMatch(updatedMatch)
                matchIdMap[oldMatchId] = newMatchId
                println("$TAG: Match ID mapping: $oldMatchId -> $newMatchId")
            }
            println("$TAG: All matches migrated successfully with ${matchIdMap.size} ID mappings")

            // Step 4: Migrate Goals with updated references
            onProgress(MigrationStep(4, TOTAL_STEPS, "Migrando goles..."))
            val goals = goalRepository.getAllLocalGoalsDirect()
            println("$TAG: Found ${goals.size} goals to migrate")
            goals.forEach { goal ->
                val updatedGoal = goal.copy(
                    matchId = matchIdMap[goal.matchId] ?: goal.matchId,
                    scorerId = goal.scorerId?.let { playerIdMap[it] ?: it }
                )
                goalRepository.insertGoal(updatedGoal)
            }
            println("$TAG: All goals migrated successfully")

            // Step 5: Migrate Player Substitutions with updated references
            onProgress(MigrationStep(5, TOTAL_STEPS, "Migrando sustituciones..."))
            val substitutions = playerSubstitutionRepository.getAllLocalPlayerSubstitutionsDirect()
            println("$TAG: Found ${substitutions.size} substitutions to migrate")
            substitutions.forEach { substitution ->
                val updatedSubstitution = substitution.copy(
                    matchId = matchIdMap[substitution.matchId] ?: substitution.matchId,
                    playerOutId = playerIdMap[substitution.playerOutId] ?: substitution.playerOutId,
                    playerInId = playerIdMap[substitution.playerInId] ?: substitution.playerInId
                )
                playerSubstitutionRepository.insertSubstitution(updatedSubstitution)
            }
            println("$TAG: All substitutions migrated successfully")

            // Step 6: Migrate Player Times
            onProgress(MigrationStep(6, TOTAL_STEPS, "Migrando tiempos de juego..."))
            val playerTimes = playerTimeRepository.getAllLocalPlayerTimesDirect()
            println("$TAG: Found ${playerTimes.size} player times to migrate")
            playerTimes.forEach { playerTime ->
                // Player times are upserted, not inserted
                // We skip this migration as current player times are transient state
            }
            println("$TAG: Skipped player times migration (transient state)")

            // Step 7: Migrate Player Time History with updated references
            onProgress(MigrationStep(7, TOTAL_STEPS, "Migrando histórico de tiempos..."))
            val timeHistory = playerTimeHistoryRepository.getAllLocalPlayerTimeHistoryDirect()
            println("$TAG: Found ${timeHistory.size} time history records to migrate")
            timeHistory.forEach { history ->
                val updatedHistory = history.copy(
                    playerId = playerIdMap[history.playerId] ?: history.playerId,
                    matchId = matchIdMap[history.matchId] ?: history.matchId
                )
                playerTimeHistoryRepository.insertPlayerTimeHistory(updatedHistory)
            }
            println("$TAG: All time history migrated successfully")

            // Step 8: Clear local data after successful migration
            onProgress(MigrationStep(8, TOTAL_STEPS, "Limpiando datos locales..."))
            println("$TAG: Clearing local Room data...")
            teamRepository.clearLocalTeamData()
            playerRepository.clearLocalPlayerData()
            matchRepository.clearLocalMatchData()
            goalRepository.clearLocalGoalData()
            playerSubstitutionRepository.clearLocalPlayerSubstitutionData()
            playerTimeRepository.clearLocalPlayerTimeData()
            playerTimeHistoryRepository.clearLocalPlayerTimeHistoryData()
            println("$TAG: Local data migration completed successfully")

            Result.success(Unit)
        } catch (e: Exception) {
            println("$TAG: Error during local data migration: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
