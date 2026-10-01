package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.usecase.ExportMatchReportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchReportDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Turns a match into a PDF report and tracks how that is going.
 *
 * Same shape as [MatchNotificationCoordinator]: plain Kotlin, no ViewModel dependency, and the
 * caller hands over the scope to work in. It touches neither the match state nor the clock, which
 * is why it can own [state] outright instead of being handed it.
 */
internal class MatchReportExporter(
    private val getMatchReportData: GetMatchReportDataUseCase,
    private val exportMatchReportToPdf: ExportMatchReportToPdfUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) {
    private val _state = MutableStateFlow<ExportState>(ExportState.Idle)
    val state: StateFlow<ExportState> = _state.asStateFlow()

    fun request(
        scope: CoroutineScope,
        matchId: String,
    ) {
        scope.launch {
            try {
                crashReporter.log("Requesting match report export for match: $matchId")
                _state.value = ExportState.Loading
                val matchReportData = getMatchReportData(matchId).firstOrNull()

                if (matchReportData != null) {
                    val uri = exportMatchReportToPdf(matchReportData)
                    _state.value =
                        if (uri != null) {
                            analyticsTracker.logEvent(
                                AnalyticsEvent.MATCH_REPORT_EXPORTED,
                                mapOf(
                                    AnalyticsParam.MATCH_ID to matchId,
                                    AnalyticsParam.EXPORT_TYPE to "pdf",
                                ),
                            )
                            ExportState.Ready(uri)
                        } else {
                            ExportState.Error
                        }
                } else {
                    _state.value = ExportState.Error
                }
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error exporting match report: ${e.message}")
                _state.value = ExportState.Error
            }
        }
    }

    fun completed() {
        _state.value = ExportState.Idle
    }
}
