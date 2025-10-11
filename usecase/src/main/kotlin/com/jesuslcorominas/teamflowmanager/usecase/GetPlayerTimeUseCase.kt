package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.Flow

interface GetPlayerTimeUseCase {
    operator fun invoke(playerId: Long): Flow<PlayerTime?>
}

internal class GetPlayerTimeUseCaseImpl(
    private val playerTimeRepository: PlayerTimeRepository,
) : GetPlayerTimeUseCase {
    override fun invoke(playerId: Long): Flow<PlayerTime?> = playerTimeRepository.getPlayerTime(playerId)
}
