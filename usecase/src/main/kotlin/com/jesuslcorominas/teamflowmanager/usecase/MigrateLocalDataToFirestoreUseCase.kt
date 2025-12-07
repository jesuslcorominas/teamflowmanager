package com.jesuslcorominas.teamflowmanager.usecase

import android.util.Log
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository

/**
 * Use case to migrate local Room data to Firebase Firestore.
 * This process includes:
 * 1. Creating a Team in Firestore with the current user as owner
 * 2. Uploading all Players to Firestore
 * 3. Uploading all Matches to Firestore
 * 4. Uploading all related statistics (Goals, Substitutions, Times)
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

            // Step 4: Clear local data after successful migration
            Log.d(TAG, "Clearing local Room data...")
            teamRepository.clearLocalTeamData()
            playerRepository.clearLocalPlayerData()
            matchRepository.clearLocalMatchData()
            Log.i(TAG, "Local data migration completed successfully")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during local data migration", e)
            Result.failure(e)
        }
    }
}
