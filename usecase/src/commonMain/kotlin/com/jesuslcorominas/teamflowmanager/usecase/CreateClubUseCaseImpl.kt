package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateClubUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository
import kotlinx.coroutines.flow.first

internal class CreateClubUseCaseImpl(
    private val clubRepository: ClubRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getTeam: GetTeamUseCase,
    private val updateTeam: UpdateTeamUseCase,
) : CreateClubUseCase {
    override suspend fun invoke(clubName: String): Club {
        // Get current authenticated user
        val currentUser =
            getCurrentUser().first()
                ?: throw IllegalStateException("User must be authenticated to create a club")

        // Validate user data
        require(currentUser.displayName?.isNotBlank() == true) {
            "User display name is required to create a club"
        }
        require(currentUser.email?.isNotBlank() == true) {
            "User email is required to create a club"
        }

        // Create club with owner
        val club =
            clubRepository.createClubWithOwner(
                clubName = clubName,
                currentUserId = currentUser.id,
                currentUserName = currentUser.displayName!!,
                currentUserEmail = currentUser.email!!,
            )

        // If user has a team, associate it with the newly created club
        val team = getTeam().first()
        if (team != null && club.firestoreId != null) {
            val updatedTeam = team.copy(clubFirestoreId = club.firestoreId)
            updateTeam(updatedTeam)
        }

        return club
    }
}
