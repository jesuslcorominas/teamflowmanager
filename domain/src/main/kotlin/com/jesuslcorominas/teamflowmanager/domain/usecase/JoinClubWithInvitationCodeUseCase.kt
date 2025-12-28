package com.jesuslcorominas.teamflowmanager.domain.usecase

import android.util.Log
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

/**
 * Use case to join a club with an invitation code.
 * This will:
 * 1. Find the club by invitation code
 * 2. Detect orphan teams owned by the user
 * 3. Link the orphan team to the club
 * 4. Create/update the club member with "Coach" role
 *
 * Note: This implementation uses Firebase caching and error handling to ensure
 * data consistency. If any operation fails, the error is thrown to allow the caller
 * to handle it appropriately.
 */
class JoinClubWithInvitationCodeUseCase(
    private val clubRepository: ClubRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val teamRepository: TeamRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) {
    companion object {
        private const val TAG = "JoinClubWithInvitationCodeUseCase"
        private const val COACH_ROLE = "Coach"
    }

    /**
     * Join a club using an invitation code.
     * @param invitationCode The invitation code for the club
     * @return Result containing the updated team and club member if successful, or error otherwise
     */
    suspend operator fun invoke(invitationCode: String): Result<JoinClubResult> {
        return try {
            // Validate invitation code
            if (invitationCode.isBlank()) {
                return Result.failure(IllegalArgumentException("Invitation code cannot be empty"))
            }

            // Get current user
            val currentUser = getCurrentUserUseCase().first()
            if (currentUser == null) {
                return Result.failure(IllegalStateException("User must be authenticated"))
            }

            // Step 1: Find club by invitation code
            val club = clubRepository.findClubByInvitationCode(invitationCode).first()
            if (club == null) {
                Log.w(TAG, "Club not found with invitation code: $invitationCode")
                return Result.failure(ClubNotFoundException("Club not found with the provided invitation code"))
            }

            Log.d(TAG, "Found club: ${club.name} (${club.firestoreId})")

            // Step 2: Get orphan teams for the current user
            val orphanTeams = teamRepository.getOrphanTeams().first()
            if (orphanTeams.isEmpty()) {
                Log.w(TAG, "No orphan teams found for user ${currentUser.uid}")
                return Result.failure(NoOrphanTeamsException("No orphan teams found to link to the club"))
            }

            Log.d(TAG, "Found ${orphanTeams.size} orphan team(s)")

            // For now, link the first orphan team
            val orphanTeam = orphanTeams.first()
            Log.d(TAG, "Linking team: ${orphanTeam.name} (${orphanTeam.coachId}) to club: ${club.name}")

            // Step 3: Update the team's clubId
            val updatedTeam = orphanTeam.copy(
                clubId = club.id,
                clubFirestoreId = club.firestoreId
            )

            try {
                teamRepository.updateTeam(updatedTeam)
                Log.d(TAG, "Team updated successfully with clubId: ${club.firestoreId}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update team", e)
                return Result.failure(TeamUpdateException("Failed to link team to club: ${e.message}", e))
            }

            // Step 4: Create/update club member with Coach role
            val clubMember = ClubMember(
                id = 0, // Will be set by data source
                userId = currentUser.id,
                name = currentUser.displayName ?: currentUser.email ?: "Unknown",
                email = currentUser.email ?: "",
                clubId = club.id,
                role = COACH_ROLE,
                firestoreId = club.firestoreId // Pass club's firestoreId to the data source
            )

            try {
                clubMemberRepository.createOrUpdateClubMember(clubMember)
                Log.d(TAG, "Club member created/updated successfully with Coach role")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create/update club member", e)
                // Team was already updated, so we need to inform the caller
                return Result.failure(ClubMemberUpdateException("Team linked but failed to update club membership: ${e.message}", e))
            }

            Result.success(JoinClubResult(updatedTeam, clubMember, club.name))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error joining club", e)
            Result.failure(e)
        }
    }

    data class JoinClubResult(
        val team: Team,
        val clubMember: ClubMember,
        val clubName: String
    )

    class ClubNotFoundException(message: String) : Exception(message)
    class NoOrphanTeamsException(message: String) : Exception(message)
    class TeamUpdateException(message: String, cause: Throwable? = null) : Exception(message, cause)
    class ClubMemberUpdateException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
