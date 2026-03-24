package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.User
import kotlinx.coroutines.flow.Flow

interface GetCurrentUserUseCase {
    operator fun invoke(): Flow<User?>
}
