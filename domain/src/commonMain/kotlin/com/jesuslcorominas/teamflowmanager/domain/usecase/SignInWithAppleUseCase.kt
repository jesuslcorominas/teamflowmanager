package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
import com.jesuslcorominas.teamflowmanager.domain.model.User

interface SignInWithAppleUseCase {
    suspend operator fun invoke(credential: AppleSignInCredential): Result<User>
}
