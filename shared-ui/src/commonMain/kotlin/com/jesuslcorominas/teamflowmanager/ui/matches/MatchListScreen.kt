package com.jesuslcorominas.teamflowmanager.ui.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.components.form.ExpandableTitle
import com.jesuslcorominas.teamflowmanager.ui.main.LocalSearchState
import com.jesuslcorominas.teamflowmanager.ui.matches.card.ArchivedMatchesNavigationCard
import com.jesuslcorominas.teamflowmanager.ui.matches.card.PausedMatchCard
import com.jesuslcorominas.teamflowmanager.ui.matches.card.PendingMatchCard
import com.jesuslcorominas.teamflowmanager.ui.matches.card.PlayedMatchCard
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchDeleteConfirmationState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.cancel
import teamflowmanager.shared_ui.generated.resources.current_match_title
import teamflowmanager.shared_ui.generated.resources.delete
import teamflowmanager.shared_ui.generated.resources.delete_match_message
import teamflowmanager.shared_ui.generated.resources.delete_match_title
import teamflowmanager.shared_ui.generated.resources.no_matches_message
import teamflowmanager.shared_ui.generated.resources.no_results
import teamflowmanager.shared_ui.generated.resources.paused_match_half_time
import teamflowmanager.shared_ui.generated.resources.pending_matches
import teamflowmanager.shared_ui.generated.resources.played_matches

@Composable
fun MatchListScreen(
    onNavigateToMatch: (Match) -> Unit = {},
    onNavigateToEditMatch: (Long) -> Unit = {},
    onNavigateToArchivedMatches: () -> Unit = {},
    viewModel: MatchListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteConfirmationState by viewModel.deleteConfirmationState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is MatchListUiState.Loading -> Loading()

            is MatchListUiState.Empty -> EmptyContent(
                text = stringResource(Res.string.no_matches_message),
            )

            is MatchListUiState.Success -> MatchesList(
                state = state,
                onNavigateToArchivedMatches = onNavigateToArchivedMatches,
                onNavigateToEditMatch = onNavigateToEditMatch,
                onNavigateToMatch = onNavigateToMatch,
                viewModel = viewModel,
            )
        }

        if (deleteConfirmationState is MatchDeleteConfirmationState.Requested) {
            AppAlertDialog(
                title = stringResource(Res.string.delete_match_title),
                message = stringResource(Res.string.delete_match_message),
                confirmText = stringResource(Res.string.delete),
                dismissText = stringResource(Res.string.cancel),
                onConfirm = { viewModel.confirmDeleteMatch() },
                onDismiss = { viewModel.cancelDeleteMatch() },
            )
        }

        if (deleteConfirmationState is MatchDeleteConfirmationState.Deleting) {
            Loading()
        }
    }
}

@Composable
private fun MatchesList(
    state: MatchListUiState.Success,
    onNavigateToArchivedMatches: () -> Unit,
    onNavigateToEditMatch: (Long) -> Unit,
    onNavigateToMatch: (Match) -> Unit,
    viewModel: MatchListViewModel,
) {
    val searchState = LocalSearchState.current

    val pendingMatches = state.matches.filter { it.status == MatchStatus.SCHEDULED }.sortedBy { it.dateTime }
    val activeMatch = state.matches.find { it.status == MatchStatus.IN_PROGRESS }
    val pausedMatch = state.matches.find { it.status == MatchStatus.PAUSED || it.status == MatchStatus.TIMEOUT }
    val playedMatches = state.matches.filter { it.status == MatchStatus.FINISHED }.sortedByDescending { it.dateTime }

    val hasActiveMatch = activeMatch != null
    val hasPausedMatch = pausedMatch != null

    var expandedPendingMatches by remember { mutableStateOf(true) }
    var expandedPlayedMatches by remember { mutableStateOf(true) }

    LaunchedEffect(searchState.query) {
        viewModel.onQueryChange(searchState.query)
        if (pendingMatches.isNotEmpty()) expandedPendingMatches = true
        if (playedMatches.isNotEmpty()) expandedPlayedMatches = true
    }

    DisposableEffect(Unit) {
        onDispose {
            searchState.isActive = false
            searchState.clear()
        }
    }

    if (state.matches.isEmpty()) {
        EmptyContent(text = stringResource(Res.string.no_results))
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = TFMSpacing.spacing04,
                start = TFMSpacing.spacing04,
                end = TFMSpacing.spacing04,
            ),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
    ) {
        item { ArchivedMatchesNavigationCard(onClick = onNavigateToArchivedMatches) }

        if (hasActiveMatch) {
            item {
                Text(
                    text = stringResource(Res.string.current_match_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = TFMSpacing.spacing02),
                )
            }
            item {
                PausedMatchCard(
                    match = activeMatch!!,
                    onResume = { onNavigateToMatch(activeMatch) },
                    onNavigateToDetail = { onNavigateToMatch(activeMatch) },
                )
            }
        }

        if (hasPausedMatch) {
            item {
                Text(
                    text = stringResource(Res.string.paused_match_half_time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = TFMSpacing.spacing02),
                )
            }
            item {
                PausedMatchCard(
                    match = pausedMatch!!,
                    onResume = {
                        viewModel.resumeMatch(pausedMatch.id)
                        onNavigateToMatch(pausedMatch)
                    },
                    onNavigateToDetail = { onNavigateToMatch(pausedMatch) },
                )
            }
        }

        if (pendingMatches.isNotEmpty()) {
            pendingMatchesSection(
                pendingMatches = pendingMatches,
                expandedPendingMatches = expandedPendingMatches,
                hasMatchStarted = hasActiveMatch || hasPausedMatch,
                onNavigateToEditMatch = onNavigateToEditMatch,
                onNavigateToMatch = onNavigateToMatch,
                onDeleteMatch = { viewModel.requestDeleteMatch(it) },
                onExpandToggle = { expandedPendingMatches = !expandedPendingMatches },
            )
        }

        if (playedMatches.isNotEmpty()) {
            playedMatchesSection(
                playedMatches = playedMatches,
                expandedPlayedMatches = expandedPlayedMatches,
                onNavigateToMatch = onNavigateToMatch,
                onArchiveMatch = { viewModel.archiveMatch(it) },
                onExpandToggle = { expandedPlayedMatches = !expandedPlayedMatches },
            )
        }

        item { Box(Modifier.height(TFMSpacing.spacing11)) }
    }
}

private fun LazyListScope.pendingMatchesSection(
    pendingMatches: List<Match>,
    expandedPendingMatches: Boolean,
    hasMatchStarted: Boolean,
    onNavigateToEditMatch: (Long) -> Unit,
    onNavigateToMatch: (Match) -> Unit,
    onDeleteMatch: (Match) -> Unit,
    onExpandToggle: () -> Unit,
) {
    item {
        ExpandableTitle(
            title = stringResource(Res.string.pending_matches),
            expanded = expandedPendingMatches,
            onClick = onExpandToggle,
        )
    }
    if (expandedPendingMatches) {
        items(pendingMatches) { match ->
            PendingMatchCard(
                match = match,
                hasMatchStarted = hasMatchStarted,
                onEdit = { onNavigateToEditMatch(match.id) },
                onDelete = { onDeleteMatch(match) },
                onStart = { onNavigateToMatch(match) },
            )
        }
    }
}

private fun LazyListScope.playedMatchesSection(
    playedMatches: List<Match>,
    expandedPlayedMatches: Boolean,
    onNavigateToMatch: (Match) -> Unit,
    onArchiveMatch: (Long) -> Unit,
    onExpandToggle: () -> Unit,
) {
    item {
        ExpandableTitle(
            title = stringResource(Res.string.played_matches),
            expanded = expandedPlayedMatches,
            onClick = onExpandToggle,
        )
    }
    if (expandedPlayedMatches) {
        items(playedMatches) { match ->
            PlayedMatchCard(
                match = match,
                onNavigateToDetail = { onNavigateToMatch(match) },
                onAction = { onArchiveMatch(match.id) },
            )
        }
    }
}
