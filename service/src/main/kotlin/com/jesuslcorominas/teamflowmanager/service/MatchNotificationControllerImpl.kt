package com.jesuslcorominas.teamflowmanager.service

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.notification.MatchNotificationController
import com.jesuslcorominas.teamflowmanager.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetActiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartTimeoutUseCase
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of MatchNotificationController that delegates to use cases.
 * This bridges the service layer with the use case layer through the domain interface.
 */
class MatchNotificationControllerImpl(
    private val getActiveMatchUseCase: GetActiveMatchUseCase,
    private val pauseMatchUseCase: PauseMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase,
    private val startTimeoutUseCase: StartTimeoutUseCase,
    private val endTimeoutUseCase: EndTimeoutUseCase,
) : MatchNotificationController {
    
    override fun getActiveMatch(): Flow<Match?> {
        return getActiveMatchUseCase()
    }

    override suspend fun pauseMatch(matchId: Long, currentTimeMillis: Long) {
        pauseMatchUseCase(matchId, currentTimeMillis)
    }

    override suspend fun resumeMatch(matchId: Long, currentTimeMillis: Long) {
        resumeMatchUseCase(matchId, currentTimeMillis)
    }

    override suspend fun startTimeout(matchId: Long, currentTimeMillis: Long) {
        startTimeoutUseCase(matchId, currentTimeMillis)
    }

    override suspend fun endTimeout(matchId: Long, currentTimeMillis: Long) {
        endTimeoutUseCase(matchId, currentTimeMillis)
    }
}
