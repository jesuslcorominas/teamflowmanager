package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.MigrateLocalDataToFirestoreUseCase
import com.jesuslcorominas.teamflowmanager.usecase.MigrationStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MigrationViewModel(
    private val migrateLocalDataToFirestoreUseCase: MigrateLocalDataToFirestoreUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Migrating(null))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed interface UiState {
        data class Migrating(val step: MigrationStep?) : UiState
        data object Success : UiState
        data class Error(val message: String) : UiState
    }

    init {
        startMigration()
    }

    private fun startMigration() {
        viewModelScope.launch {
            try {
                // Get current user ID
                val user = getCurrentUserUseCase().first()
                if (user == null) {
                    analyticsTracker.logEvent(
                        "migration_error",
                        mapOf("error" to "No authenticated user")
                    )
                    _uiState.value = UiState.Error("Usuario no autenticado")
                    return@launch
                }

                analyticsTracker.logEvent("migration_started", emptyMap())

                // Execute migration with progress callback
                migrateLocalDataToFirestoreUseCase(user.id) { step ->
                    _uiState.value = UiState.Migrating(step)
                }
                    .onSuccess {
                        analyticsTracker.logEvent("migration_completed", emptyMap())
                        _uiState.value = UiState.Success
                    }
                    .onFailure { exception ->
                        analyticsTracker.logEvent(
                            "migration_error",
                            mapOf("error" to (exception.message ?: "Unknown error"))
                        )
                        _uiState.value = UiState.Error(
                            exception.message ?: "Error al migrar los datos"
                        )
                    }
            } catch (e: Exception) {
                analyticsTracker.logEvent(
                    "migration_error",
                    mapOf("error" to (e.message ?: "Unknown error"))
                )
                _uiState.value = UiState.Error(e.message ?: "Error al migrar los datos")
            }
        }
    }
}
