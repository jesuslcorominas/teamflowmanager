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

            // Step 2: Migrate Players
            val players = playerRepository.getAllLocalPlayersDirect()
            Log.d(TAG, "Found ${players.size} players to migrate")
            players.forEach { player ->
                playerRepository.addPlayer(player)
            }
            Log.d(TAG, "All players migrated successfully")

            // Step 3: Migrate Matches
            val matches = matchRepository.getAllLocalMatchesDirect()
            Log.d(TAG, "Found ${matches.size} matches to migrate")
            matches.forEach { match ->
                matchRepository.createMatch(match)
            }
            Log.d(TAG, "All matches migrated successfully")

            // Step 4: Migrate Goals
            val goals = goalRepository.getAllLocalGoalsDirect()
            Log.d(TAG, "Found ${goals.size} goals to migrate")
            goals.forEach { goal ->
                goalRepository.insertGoal(goal)
            }
            Log.d(TAG, "All goals migrated successfully")

            // Step 5: Migrate Player Substitutions
            val substitutions = playerSubstitutionRepository.getAllLocalPlayerSubstitutionsDirect()
            Log.d(TAG, "Found ${substitutions.size} substitutions to migrate")
            substitutions.forEach { substitution ->
                playerSubstitutionRepository.insertSubstitution(substitution)
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

            // Step 7: Migrate Player Time History
            val timeHistory = playerTimeHistoryRepository.getAllLocalPlayerTimeHistoryDirect()
            Log.d(TAG, "Found ${timeHistory.size} time history records to migrate")
            timeHistory.forEach { history ->
                playerTimeHistoryRepository.insertPlayerTimeHistory(history)
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
