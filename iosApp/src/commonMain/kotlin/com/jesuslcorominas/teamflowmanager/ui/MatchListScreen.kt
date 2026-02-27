package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Phase 2 iOS MVP Match list screen.
 * Shows opponent + score as simple Text — UI polish deferred to Phase 3.
 */
@Composable
fun MatchListScreen() {
    val viewModel: MatchListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Partidos")

        when (val state = uiState) {
            is MatchListUiState.Loading -> Text("Cargando…")
            is MatchListUiState.Empty -> Text("No hay partidos")
            is MatchListUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.matches) { match ->
                        Text(
                            text = "${match.opponent}  ${match.goals} – ${match.opponentGoals}",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
