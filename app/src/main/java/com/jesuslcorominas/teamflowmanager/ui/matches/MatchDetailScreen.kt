package com.jesuslcorominas.teamflowmanager.ui.matches

import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.ui.components.AppTextField
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchDetailUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchDetailViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: Long?,
    onNavigateBack: () -> Unit,
    detailViewModel: MatchDetailViewModel = koinViewModel(),
    listViewModel: MatchListViewModel = koinViewModel(),
) {
    val uiState by detailViewModel.uiState.collectAsState()

    LaunchedEffect(matchId) {
        if (matchId != null) {
            detailViewModel.loadMatch(matchId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.edit_match_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.close),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is MatchDetailUiState.Loading -> {
                CircularProgressIndicator(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                )
            }

            is MatchDetailUiState.NotFound -> {
                Text(
                    text = stringResource(R.string.no_match_message),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(TFMSpacing.spacing04),
                )
            }

            is MatchDetailUiState.Edit -> {
                MatchForm(
                    match = state.match,
                    availablePlayers = state.availablePlayers,
                    onSave = { match ->
                        listViewModel.updateMatch(match)
                        onNavigateBack()
                    },
                    onCancel = onNavigateBack,
                    modifier = Modifier.padding(paddingValues),
                )
            }

            is MatchDetailUiState.Create -> {
                // Should not happen anymore, but keeping for safety
                Text(
                    text = "Please use the match creation wizard",
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(TFMSpacing.spacing04),
                )
            }
        }
    }
}

@Composable
fun MatchForm(
    match: Match?,
    availablePlayers: List<Player>,
    onSave: (Match) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var opponent by remember { mutableStateOf(match?.opponent ?: "") }
    var location by remember { mutableStateOf(match?.location ?: "") }
    var selectedStartingLineup by remember {
        mutableStateOf(
            match?.startingLineupIds?.toSet() ?: emptySet(),
        )
    }
    var selectedSubstitutes by remember {
        mutableStateOf(
            match?.substituteIds?.toSet() ?: emptySet(),
        )
    }
    var opponentError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(TFMSpacing.spacing04),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing04),
    ) {
        AppTextField(
            value = opponent,
            onValueChange = {
                opponent = it
                opponentError = null
            },
            label = { Text(stringResource(R.string.opponent)) },
            isError = opponentError != null,
            supportingText = if (opponentError != null) {
                { Text(opponentError!!) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
        )

        AppTextField(
            value = location,
            onValueChange = {
                location = it
                locationError = null
            },
            label = { Text(stringResource(R.string.location)) },
            isError = locationError != null,
            supportingText = if (locationError != null) {
                { Text(locationError!!) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(R.string.select_starting_lineup),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(TFMSpacing.spacing18),
            ) {
                items(availablePlayers) { player ->
                    PlayerSelectionItem(
                        player = player,
                        isSelected = selectedStartingLineup.contains(player.id),
                        onSelectionChange = { isSelected ->
                            selectedStartingLineup =
                                if (isSelected) {
                                    selectedStartingLineup + player.id
                                } else {
                                    selectedStartingLineup - player.id
                                }
                        },
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.select_substitutes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(TFMSpacing.spacing18),
            ) {
                items(availablePlayers) { player ->
                    PlayerSelectionItem(
                        player = player,
                        isSelected = selectedSubstitutes.contains(player.id),
                        onSelectionChange = { isSelected ->
                            selectedSubstitutes =
                                if (isSelected) {
                                    selectedSubstitutes + player.id
                                } else {
                                    selectedSubstitutes - player.id
                                }
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.cancel))
            }

            Button(
                onClick = {
                    var hasError = false
                    if (opponent.isBlank()) {
                        opponentError = "Required"
                        hasError = true
                    }
                    if (location.isBlank()) {
                        locationError = "Required"
                        hasError = true
                    }

                    if (!hasError) {
                        val newMatch =
                            Match(
                                id = match?.id ?: 0L,
                                teamId = match?.teamId ?: 1L,
                                opponent = opponent,
                                location = location,
                                date = match?.date,
                                time = match?.time,
                                numberOfPeriods = match?.numberOfPeriods ?: 2,
                                periodDurationMinutes = match?.periodDurationMinutes ?: 25,
                                squadCallUpIds = match?.squadCallUpIds ?: emptyList(),
                                captainId = match?.captainId,
                                startingLineupIds = selectedStartingLineup.toList(),
                                substituteIds = selectedSubstitutes.toList(),
                                elapsedTimeMillis = match?.elapsedTimeMillis ?: 0L,
                                isRunning = match?.isRunning ?: false,
                                lastStartTimeMillis = match?.lastStartTimeMillis,
                            )
                        onSave(newMatch)
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
fun PlayerSelectionItem(
    player: Player,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onSelectionChange(!isSelected) }
                .padding(TFMSpacing.spacing02),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelectionChange,
        )
        Column(
            modifier = Modifier.padding(start = TFMSpacing.spacing02),
        ) {
            Text(
                text = "${player.number} - ${player.firstName} ${player.lastName}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
