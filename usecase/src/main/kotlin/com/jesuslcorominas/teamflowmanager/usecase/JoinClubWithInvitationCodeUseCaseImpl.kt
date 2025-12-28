package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubWithInvitationCodeUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

/**
 * Implementation of JoinClubWithInvitationCodeUseCase.
 * Orchestrates the process of joining a club using an invitation code.
 */
internal class JoinClubWithInvitationCodeUseCaseImpl(
    private val clubRepository: ClubRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val teamRepository: TeamRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : JoinClubWithInvitationCodeUseCase {

    companion object {
        private const val COACH_ROLE = "Coach"
    }

    override suspend fun invoke(invitationCode: String): Result<JoinClubWithInvitationCodeUseCase.JoinClubResult> {
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
                return Result.failure(
                    JoinClubWithInvitationCodeUseCase.ClubNotFoundException(
                        "Club not found with the provided invitation code"
                    )
                )
            }

            // Step 2: Get orphan teams for the current user
            val orphanTeams = teamRepository.getOrphanTeams().first()
            if (orphanTeams.isEmpty()) {
                return Result.failure(
                    JoinClubWithInvitationCodeUseCase.NoOrphanTeamsException(
                        "No orphan teams found to link to the club"
                    )
                )
            }

            // For now, link the first orphan team
            val orphanTeam = orphanTeams.first()

            // Step 3: Update the team's clubId
            val updatedTeam = orphanTeam.copy(
                clubId = club.id,
                clubFirestoreId = club.firestoreId
            )

            try {
                teamRepository.updateTeam(updatedTeam)
            } catch (e: Exception) {
                return Result.failure(
                    JoinClubWithInvitationCodeUseCase.TeamUpdateException(
                        "Failed to link team to club: ${e.message}",
                        e
                    )
                )
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
            } catch (e: Exception) {
                // Team was already updated, so we need to inform the caller
                return Result.failure(
                    JoinClubWithInvitationCodeUseCase.ClubMemberUpdateException(
                        "Team linked but failed to update club membership: ${e.message}",
                        e
                    )
                )
            }

            Result.success(
                JoinClubWithInvitationCodeUseCase.JoinClubResult(
                    updatedTeam,
                    clubMember,
                    club.name
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
