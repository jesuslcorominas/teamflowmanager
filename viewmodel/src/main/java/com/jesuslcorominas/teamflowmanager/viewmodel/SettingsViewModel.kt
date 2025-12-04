package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.usecase.ExportDatabaseUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ImportDatabaseUseCase
import com.jesuslcorominas.teamflowmanager.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val exportDatabaseUseCase: ExportDatabaseUseCase,
    private val importDatabaseUseCase: ImportDatabaseUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    val currentUser: StateFlow<User?> = getCurrentUserUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _signOutComplete = MutableStateFlow(false)
    val signOutComplete: StateFlow<Boolean> = _signOutComplete.asStateFlow()

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

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            analyticsTracker.logEvent("logout", emptyMap())
            analyticsTracker.setUserId(null)
            _signOutComplete.value = true
        }
    }

    fun clearSignOutComplete() {
        _signOutComplete.value = false
    }
}
