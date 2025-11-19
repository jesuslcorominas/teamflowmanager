package com.jesuslcorominas.teamflowmanager.viewmodel

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
                } else {
                    _exportResult.value = Result.failure(Exception("Failed to export database"))
                }
            } catch (e: Exception) {
                _exportResult.value = Result.failure(e)
            }
        }
    }

    fun importData(fileUri: String) {
        viewModelScope.launch {
            try {
                val success = importDatabaseUseCase(fileUri)
                if (success) {
                    _importResult.value = Result.success(true)
                } else {
                    _importResult.value = Result.failure(Exception("Failed to import database"))
                }
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
