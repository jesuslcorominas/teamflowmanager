package com.jesuslcorominas.teamflowmanager.ui.matches

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.ui.components.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.DateFormatter
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchDeleteConfirmationState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MatchListScreen(
    onNavigateToAddMatch: () -> Unit,
    onNavigateToEditMatch: (Long) -> Unit,
    viewModel: MatchListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteConfirmationState by viewModel.deleteConfirmationState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddMatch) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_match_title),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            when (val state = uiState) {
                is MatchListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is MatchListUiState.Empty -> {
                    Text(
                        text = stringResource(R.string.no_matches_message),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(TFMSpacing.medium),
                    )
                }

                is MatchListUiState.Success -> {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(TFMSpacing.medium),
                        verticalArrangement = Arrangement.spacedBy(TFMSpacing.small),
                    ) {
                        items(state.matches) { match ->
                            MatchCard(
                                match = match,
                                onEdit = { onNavigateToEditMatch(match.id) },
                                onDelete = { viewModel.requestDeleteMatch(match) },
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (deleteConfirmationState is MatchDeleteConfirmationState.Requested) {
            AppAlertDialog(
                title = stringResource(R.string.delete_match_title),
                message = stringResource(R.string.delete_match_message),
                confirmText = stringResource(R.string.delete),
                dismissText = stringResource(R.string.cancel),
                onConfirm = { viewModel.confirmDeleteMatch() },
                onDismiss = { viewModel.cancelDeleteMatch() },
            )
        }
    }
}

@Composable
fun MatchCard(
    match: Match,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TFMSpacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = match.opponent ?: stringResource(R.string.opponent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(TFMSpacing.extraSmall))
                Text(
                    text = match.location ?: stringResource(R.string.location),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (match.date != null) {
                    Spacer(modifier = Modifier.height(TFMSpacing.extraSmall))
                    Text(
                        text = DateFormatter.formatDateTime(match.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(TFMSpacing.extraSmall))
                Text(
                    text =
                        stringResource(
                            R.string.starting_lineup,
                        ) + ": ${match.startingLineupIds.size}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(R.string.substitutes) + ": ${match.substituteIds.size}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                    )
                }
            }
        }
    }
}
