package com.jesuslcorominas.teamflowmanager.usecase

import android.util.Log
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository

/**
 * Use case to migrate local Room data to Firebase Firestore.
 * This process includes:
 * 1. Creating a Team in Firestore with the current user as owner
 * 2. Uploading all Players to Firestore
 * 3. Uploading all Matches to Firestore
 * 4. Uploading all related statistics (Goals, Substitutions, Player Times, Player Time History)
 * 5. Clearing local Room data after successful upload
 */
interface MigrateLocalDataToFirestoreUseCase {
    /**
     * Execute the migration process.
     * @param userId The current authenticated user's ID
     * @return Result indicating success or failure with error message
     */
    suspend operator fun invoke(userId: String): Result<Unit>
}

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
    }

    override suspend fun invoke(userId: String): Result<Unit> {
        return try {
            Log.i(TAG, "Starting local data migration to Firestore for user: $userId")

            // Step 1: Migrate Team
            val team = teamRepository.getLocalTeamDirect()
            if (team == null) {
                Log.w(TAG, "No local team found to migrate")
                return Result.failure(IllegalStateException("No local team found"))
            }

            Log.d(TAG, "Found local team: ${team.name}, migrating...")
            // Create team in Firestore with userId as coachId
            val teamWithCoachId = team.copy(coachId = userId)
            teamRepository.createTeam(teamWithCoachId)
            Log.d(TAG, "Team migrated successfully")

            // Step 2: Migrate Players and build ID mapping
            val players = playerRepository.getAllLocalPlayersDirect()
            Log.d(TAG, "Found ${players.size} players to migrate")
            val playerIdMap = mutableMapOf<Long, Long>() // old ID -> new ID
            
            players.forEach { player ->
                val oldId = player.id
                val newId = playerRepository.addPlayer(player)
                playerIdMap[oldId] = newId
                Log.d(TAG, "Player ID mapping: $oldId -> $newId")
            }
            Log.d(TAG, "All players migrated successfully with ${playerIdMap.size} ID mappings")

            // Step 3: Migrate Matches and build ID mapping, updating player references
            val matches = matchRepository.getAllLocalMatchesDirect()
            Log.d(TAG, "Found ${matches.size} matches to migrate")
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
                Log.d(TAG, "Match ID mapping: $oldMatchId -> $newMatchId")
            }
            Log.d(TAG, "All matches migrated successfully with ${matchIdMap.size} ID mappings")

            // Step 4: Migrate Goals with updated references
            val goals = goalRepository.getAllLocalGoalsDirect()
            Log.d(TAG, "Found ${goals.size} goals to migrate")
            goals.forEach { goal ->
                val updatedGoal = goal.copy(
                    matchId = matchIdMap[goal.matchId] ?: goal.matchId,
                    scorerId = goal.scorerId?.let { playerIdMap[it] ?: it }
                )
                goalRepository.insertGoal(updatedGoal)
            }
            Log.d(TAG, "All goals migrated successfully")

            // Step 5: Migrate Player Substitutions with updated references
            val substitutions = playerSubstitutionRepository.getAllLocalPlayerSubstitutionsDirect()
            Log.d(TAG, "Found ${substitutions.size} substitutions to migrate")
            substitutions.forEach { substitution ->
                val updatedSubstitution = substitution.copy(
                    matchId = matchIdMap[substitution.matchId] ?: substitution.matchId,
                    playerOutId = playerIdMap[substitution.playerOutId] ?: substitution.playerOutId,
                    playerInId = playerIdMap[substitution.playerInId] ?: substitution.playerInId
                )
                playerSubstitutionRepository.insertSubstitution(updatedSubstitution)
            }
            Log.d(TAG, "All substitutions migrated successfully")

            // Step 6: Migrate Player Times
            val playerTimes = playerTimeRepository.getAllLocalPlayerTimesDirect()
            Log.d(TAG, "Found ${playerTimes.size} player times to migrate")
            playerTimes.forEach { playerTime ->
                // Player times are upserted, not inserted
                // We skip this migration as current player times are transient state
            }
            Log.d(TAG, "Skipped player times migration (transient state)")

            // Step 7: Migrate Player Time History with updated references
            val timeHistory = playerTimeHistoryRepository.getAllLocalPlayerTimeHistoryDirect()
            Log.d(TAG, "Found ${timeHistory.size} time history records to migrate")
            timeHistory.forEach { history ->
                val updatedHistory = history.copy(
                    playerId = playerIdMap[history.playerId] ?: history.playerId,
                    matchId = matchIdMap[history.matchId] ?: history.matchId
                )
                playerTimeHistoryRepository.insertPlayerTimeHistory(updatedHistory)
            }
            Log.d(TAG, "All time history migrated successfully")

            // Step 8: Clear local data after successful migration
            Log.d(TAG, "Clearing local Room data...")
            teamRepository.clearLocalTeamData()
            playerRepository.clearLocalPlayerData()
            matchRepository.clearLocalMatchData()
            goalRepository.clearLocalGoalData()
            playerSubstitutionRepository.clearLocalPlayerSubstitutionData()
            playerTimeRepository.clearLocalPlayerTimeData()
            playerTimeHistoryRepository.clearLocalPlayerTimeHistoryData()
            Log.i(TAG, "Local data migration completed successfully")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during local data migration", e)
            Result.failure(e)
        }
    }
}
