package com.jesuslcorominas.teamflowmanager.domain.usecase

import kotlinx.coroutines.flow.Flow

interface GetUnreadPresidentNotificationsCountUseCase {
    operator fun invoke(clubId: String): Flow<Int>
}
