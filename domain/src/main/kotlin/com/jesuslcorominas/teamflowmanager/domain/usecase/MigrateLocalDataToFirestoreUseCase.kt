package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MigrationStep

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
     * @param onProgress Callback to report migration progress
     * @return Result indicating success or failure with error message
     */
    suspend operator fun invoke(
        userId: String,
        onProgress: (MigrationStep) -> Unit = {}
    ): Result<Unit>
}
