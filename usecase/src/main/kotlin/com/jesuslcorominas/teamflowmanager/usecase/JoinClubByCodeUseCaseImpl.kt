package com.jesuslcorominas.teamflowmanager.usecase

import android.util.Log
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubByCodeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubResult
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class JoinClubByCodeUseCaseImpl(
    private val clubRepository: ClubRepository,
    private val teamRepository: TeamRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
) : JoinClubByCodeUseCase {

    companion object {
        private const val TAG = "JoinClubByCodeUseCase"
        private const val ROLE_COACH = "Coach"
        private const val ROLE_MIEMBRO = "Miembro"
    }

    override suspend fun invoke(invitationCode: String): JoinClubResult {
        // Validate invitation code
        require(invitationCode.isNotBlank()) {
            "Invitation code cannot be blank"
        }

        // Get current authenticated user
        val currentUser = getCurrentUser().first()
            ?: throw IllegalStateException("User must be authenticated to join a club")

        // Validate user data
        require(currentUser.displayName?.isNotBlank() == true) {
            "User display name is required to join a club"
        }
        require(currentUser.email?.isNotBlank() == true) {
            "User email is required to join a club"
        }

        // Find club by invitation code
        val club = clubRepository.getClubByInvitationCode(invitationCode)
            ?: throw IllegalArgumentException("Club not found with invitation code: $invitationCode")

        Log.d(TAG, "Found club: ${club.name} (id: ${club.id}, firestoreId: ${club.firestoreId})")

        require(club.firestoreId != null) {
            "Club firestore ID is required"
        }

        // Get orphan teams (teams without clubId) for current user
        val orphanTeams = teamRepository.getOrphanTeams(currentUser.id)
        val orphanTeam = orphanTeams.firstOrNull()

        if (orphanTeam != null) {
            Log.d(TAG, "Found orphan team: ${orphanTeam.name} (coachId: ${orphanTeam.coachId})")
        } else {
            Log.d(TAG, "No orphan team found for user")
        }

        // Determine role based on whether user has an orphan team
        val role = if (orphanTeam != null) ROLE_COACH else ROLE_MIEMBRO

        try {
            // Step 1: Link orphan team to club if exists
            if (orphanTeam != null && orphanTeam.coachId != null) {
                Log.d(TAG, "Linking orphan team to club...")
                teamRepository.updateTeamClubId(
                    teamCoachId = orphanTeam.coachId!!,
                    clubId = club.id,
                    clubFirestoreId = club.firestoreId!!
                )
                Log.d(TAG, "Team linked successfully")
            }

            // Step 2: Create or update club member with appropriate role
            Log.d(TAG, "Creating club member with role: $role")
            val clubMember = clubMemberRepository.createOrUpdateClubMember(
                userId = currentUser.id,
                name = currentUser.displayName!!,
                email = currentUser.email!!,
                clubId = club.id,
                clubFirestoreId = club.firestoreId!!,
                role = role
            )
            Log.d(TAG, "Club member created successfully")

            return JoinClubResult(
                club = club,
                orphanTeam = orphanTeam,
                clubMember = clubMember
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during club join process", e)
            
            // If club member creation failed but team was linked, we have inconsistent data
            // Log this for manual investigation
            if (orphanTeam != null && orphanTeam.coachId != null) {
                Log.e(TAG, "INCONSISTENT STATE: Team ${orphanTeam.coachId} may be linked to club ${club.firestoreId} but clubMember creation failed")
            }
            
            throw e
        }
    }
}
