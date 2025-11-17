package com.jesuslcorominas.teamflowmanager.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.usecase.ExportDatabaseUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ImportDatabaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val exportDatabaseUseCase: ExportDatabaseUseCase,
    private val importDatabaseUseCase: ImportDatabaseUseCase
) : ViewModel() {

    private val _exportResult = MutableStateFlow<Result<Unit>?>(null)
    val exportResult: StateFlow<Result<Unit>?> = _exportResult.asStateFlow()

    private val _importResult = MutableStateFlow<Result<Unit>?>(null)
    val importResult: StateFlow<Result<Unit>?> = _importResult.asStateFlow()

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                exportDatabaseUseCase(context, uri)
                _exportResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _exportResult.value = Result.failure(e)
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                importDatabaseUseCase(context, uri)
                _importResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _importResult.value = Result.failure(e)
            }
        }
    }

    fun clearExportResult() {
        _exportResult.value = null
    }

    fun clearImportResult() {
        _importResult.value = null
    }
}
