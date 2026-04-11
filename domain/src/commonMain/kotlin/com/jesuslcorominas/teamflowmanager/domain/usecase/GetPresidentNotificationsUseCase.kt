package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import kotlinx.coroutines.flow.Flow

interface GetPresidentNotificationsUseCase {
    operator fun invoke(clubId: String): Flow<List<PresidentNotification>>
}
