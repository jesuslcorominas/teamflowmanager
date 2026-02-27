package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Phase 2 iOS MVP Login screen.
 * Google Sign-In will be implemented in KMP-17.
 * For now displays authentication state and provides a sign-in stub.
 */
@Composable
fun LoginScreen() {
    val viewModel: LoginViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("TeamFlow Manager")

        Spacer(Modifier.height(16.dp))

        when (val state = uiState) {
            is LoginViewModel.UiState.Idle -> {
                Text("Inicia sesión para continuar")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { /* Google Sign-In: KMP-17 */ }) {
                    Text("Sign in (KMP-17)")
                }
            }
            is LoginViewModel.UiState.Loading -> Text("Iniciando sesión…")
            is LoginViewModel.UiState.Success -> Text("Sesión iniciada")
            is LoginViewModel.UiState.Error -> {
                Text("Error: ${state.message}")
                Button(onClick = { viewModel.resetState() }) {
                    Text("Reintentar")
                }
            }
        }
    }
}
