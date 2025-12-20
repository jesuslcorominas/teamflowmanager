package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.User

interface SignInWithGoogleUseCase {
    suspend operator fun invoke(idToken: String): Result<User>
}
