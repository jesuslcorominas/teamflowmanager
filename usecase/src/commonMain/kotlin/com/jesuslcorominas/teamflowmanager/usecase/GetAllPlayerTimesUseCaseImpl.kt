package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.Flow



internal class GetAllPlayerTimesUseCaseImpl(
    private val playerTimeRepository: PlayerTimeRepository,
) : GetAllPlayerTimesUseCase {
    override fun invoke(): Flow<List<PlayerTime>> = playerTimeRepository.getAllPlayerTimes()
}
