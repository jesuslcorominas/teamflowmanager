package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club

interface GetClubByFirestoreIdUseCase {
    suspend operator fun invoke(firestoreId: String): Club?
}
