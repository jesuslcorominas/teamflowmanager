package com.jesuslcorominas.teamflowmanager.service

import android.content.Context
import android.content.Intent
import android.os.Build
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.usecase.GetActiveMatchUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MatchNotificationServiceManager(
    private val context: Context,
    private val getActiveMatchUseCase: GetActiveMatchUseCase,
    private val scope: CoroutineScope,
) {
    private var observerJob: Job? = null

    fun start() {
        observerJob?.cancel()
        observerJob =
            scope.launch {
                getActiveMatchUseCase()
                    .map { it?.isActiveForNotification() ?: false }
                    .distinctUntilChanged()
                    .collect { hasActiveMatch ->
                        if (hasActiveMatch) {
                            startService()
                        } else {
                            stopService()
                        }
                    }
            }
    }

    fun stop() {
        observerJob?.cancel()
        stopService()
    }

    private fun startService() {
        val intent =
            Intent(context, MatchCountdownService::class.java).apply {
                action = MatchCountdownService.ACTION_START_SERVICE
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopService() {
        val intent =
            Intent(context, MatchCountdownService::class.java).apply {
                action = MatchCountdownService.ACTION_STOP_SERVICE
            }
        context.startService(intent)
    }

    private fun Match.isActiveForNotification(): Boolean {
        return status == MatchStatus.IN_PROGRESS ||
            status == MatchStatus.PAUSED ||
            status == MatchStatus.TIMEOUT
    }
}
