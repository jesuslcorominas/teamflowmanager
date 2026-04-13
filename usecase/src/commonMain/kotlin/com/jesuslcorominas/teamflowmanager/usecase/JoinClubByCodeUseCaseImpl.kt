package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubByCodeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubResult
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentOnMemberWaitingUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class JoinClubByCodeUseCaseImpl(
    private val clubRepository: ClubRepository,
    private val teamRepository: TeamRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val notifyPresidentOnMemberWaiting: NotifyPresidentOnMemberWaitingUseCase,
) : JoinClubByCodeUseCase {
    companion object {
        private const val ROLE_COACH = "Coach"
        private const val ROLE_STAFF = "Staff"
    }

    override suspend fun invoke(invitationCode: String): JoinClubResult {
        // Validate invitation code
        require(invitationCode.isNotBlank()) {
            "Invitation code cannot be blank"
        }

        // Get current authenticated user
        val currentUser =
            getCurrentUser().first()
                ?: throw IllegalStateException("User must be authenticated to join a club")

        // Validate user data
        require(currentUser.displayName?.isNotBlank() == true) {
            "User display name is required to join a club"
        }
        require(currentUser.email?.isNotBlank() == true) {
            "User email is required to join a club"
        }

        // Find club by invitation code
        val club =
            clubRepository.getClubByInvitationCode(invitationCode)
                ?: throw IllegalArgumentException("Club not found with invitation code: $invitationCode")

        require(club.remoteId != null) {
            "Club remote ID is required"
        }

        // Get orphan teams (teams without clubId) for current user
        val orphanTeams = teamRepository.getOrphanTeams(currentUser.id)
        val orphanTeam = orphanTeams.firstOrNull()

        // Determine role based on whether user has an orphan team
        val roles = if (orphanTeam != null) listOf(ROLE_COACH) else listOf(ROLE_STAFF)

        try {
            // Step 1: Link orphan team to club if exists
            if (orphanTeam != null) {
                require(orphanTeam.remoteId != null) {
                    "Orphan team must have a remote ID"
                }
                teamRepository.updateTeamClubId(
                    teamId = orphanTeam.remoteId!!,
                    clubNumericId = club.id,
                    clubId = club.remoteId!!,
                )
            }

            // Step 2: Create or update club member with appropriate role
            val clubMember =
                clubMemberRepository.createOrUpdateClubMember(
                    userId = currentUser.id,
                    name = currentUser.displayName!!,
                    email = currentUser.email!!,
                    clubNumericId = club.id,
                    clubId = club.remoteId!!,
                    roles = roles,
                )

            if (orphanTeam == null) {
                try {
                    notifyPresidentOnMemberWaiting(
                        clubId = club.remoteId!!,
                        presidentUserId = club.ownerId,
                        userName = currentUser.displayName!!,
                        userEmail = currentUser.email!!,
                    )
                } catch (e: Exception) {
                    // Notification failure must not prevent the user from joining the club
                }
            }

            return JoinClubResult(
                club = club,
                orphanTeam = orphanTeam,
                clubMember = clubMember,
            )
        } catch (e: Exception) {
            // If club member creation failed but team was linked, we have inconsistent data
            // This requires manual investigation
            throw e
        }
    }
}
