package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

interface SignInWithGoogleUseCase {
    suspend operator fun invoke(idToken: String): Result<User>
}

internal class SignInWithGoogleUseCaseImpl(
    private val authRepository: AuthRepository,
    private val teamRepository: TeamRepository,
) : SignInWithGoogleUseCase {
    override suspend fun invoke(idToken: String): Result<User> {
        val result = authRepository.signInWithGoogle(idToken)
        result.getOrNull()?.let { user ->
            authRepository.saveUserToFirestore(user)

            // Check if team exists for this user, create one if it doesn't
            val existingTeam = teamRepository.getTeamByCoachId(user.id).first()
            if (existingTeam == null) {
                // Create an initial team for the new coach
                val newTeam = Team(
                    id = 0,
                    name = "",
                    coachName = user.displayName ?: "",
                    delegateName = "",
                    captainId = null,
                    teamType = TeamType.FOOTBALL_5,
                    coachId = user.id,
                )
                teamRepository.createTeam(newTeam)
            }
        }
        return result
    }
}
