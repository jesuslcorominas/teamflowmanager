package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.usecase.ExportDatabaseUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ImportDatabaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val exportDatabaseUseCase: ExportDatabaseUseCase,
    private val importDatabaseUseCase: ImportDatabaseUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _exportResult = MutableStateFlow<Result<String?>?>(null)
    val exportResult: StateFlow<Result<String?>?> = _exportResult.asStateFlow()

    private val _importResult = MutableStateFlow<Result<Boolean>?>(null)
    val importResult: StateFlow<Result<Boolean>?> = _importResult.asStateFlow()

    fun exportData() {
        viewModelScope.launch {
            try {
                val fileUri = exportDatabaseUseCase()
                if (fileUri != null) {
                    _exportResult.value = Result.success(fileUri)
                    // Track successful export
                    analyticsTracker.logEvent(
                        AnalyticsEvent.DATABASE_EXPORTED,
                        mapOf(
                            AnalyticsParam.EXPORT_SUCCESS to true
                        )
                    )
                } else {
                    _exportResult.value = Result.failure(Exception("Failed to export database"))
                    // Track failed export
                    analyticsTracker.logEvent(
                        AnalyticsEvent.DATABASE_EXPORTED,
                        mapOf(
                            AnalyticsParam.EXPORT_SUCCESS to false,
                            AnalyticsParam.ERROR_MESSAGE to "Export returned null"
                        )
                    )
                }
            } catch (e: Exception) {
                _exportResult.value = Result.failure(e)
                // Track failed export
                analyticsTracker.logEvent(
                    AnalyticsEvent.DATABASE_EXPORTED,
                    mapOf(
                        AnalyticsParam.EXPORT_SUCCESS to false,
                        AnalyticsParam.ERROR_MESSAGE to (e.message ?: "Unknown error")
                    )
                )
            }
        }
    }

    fun importData(fileUri: String, source: String) {
        viewModelScope.launch {
            try {
                val success = importDatabaseUseCase(fileUri)
                if (success) {
                    _importResult.value = Result.success(true)
                    // Track successful import
                    analyticsTracker.logEvent(
                        AnalyticsEvent.DATABASE_IMPORTED,
                        mapOf(
                            AnalyticsParam.IMPORT_SOURCE to source,
                            AnalyticsParam.IMPORT_SUCCESS to true
                        )
                    )
                } else {
                    _importResult.value = Result.failure(Exception("Failed to import database"))
                    // Track failed import
                    analyticsTracker.logEvent(
                        AnalyticsEvent.DATABASE_IMPORTED,
                        mapOf(
                            AnalyticsParam.IMPORT_SOURCE to source,
                            AnalyticsParam.IMPORT_SUCCESS to false,
                            AnalyticsParam.ERROR_MESSAGE to "Import returned false"
                        )
                    )
                }
            } catch (e: Exception) {
                _importResult.value = Result.failure(e)
                // Track failed import
                analyticsTracker.logEvent(
                    AnalyticsEvent.DATABASE_IMPORTED,
                    mapOf(
                        AnalyticsParam.IMPORT_SOURCE to source,
                        AnalyticsParam.IMPORT_SUCCESS to false,
                        AnalyticsParam.ERROR_MESSAGE to (e.message ?: "Unknown error")
                    )
                )
            }
        }
    }

    fun trackImportCancelled(source: String) {
        analyticsTracker.logEvent(
            AnalyticsEvent.DATABASE_IMPORT_CANCELLED,
            mapOf(
                AnalyticsParam.IMPORT_SOURCE to source
            )
        )
    }

    fun clearExportResult() {
        _exportResult.value = null
    }

    fun clearImportResult() {
        _importResult.value = null
    }
}
