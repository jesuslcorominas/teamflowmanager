package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Player

interface GetCaptainPlayerUseCase {
    suspend operator fun invoke(): Player?
}
