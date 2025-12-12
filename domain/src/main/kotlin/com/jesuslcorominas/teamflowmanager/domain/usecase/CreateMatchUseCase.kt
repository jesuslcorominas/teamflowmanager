package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SkeletonMatch

interface CreateMatchUseCase {
    suspend operator fun invoke(skeleton: SkeletonMatch): Long
}
