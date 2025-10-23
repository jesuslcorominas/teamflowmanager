package com.jesuslcorominas.teamflowmanager.ui.matches

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTextField
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerItem
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchDetailUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchDetailViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import org.koin.androidx.compose.koinViewModel

// TODO delete this screen

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
            is MatchDetailUiState.Loading -> Loading()

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
    val focusManager = LocalFocusManager.current

    var opponent by remember { mutableStateOf(match?.opponent ?: "") }
    var location by remember { mutableStateOf(match?.location ?: "") }
    var selectedStartingLineup by remember {
        mutableStateOf(
            match?.startingLineupIds?.toSet() ?: emptySet(),
        )
    }
    var selectedSubstitutes by remember { mutableStateOf(emptySet<Long>()) }
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
            modifier = Modifier.fillMaxWidth(),
            value = opponent,
            onValueChange = {
                opponent = it
                opponentError = null
            },
            label = { Text(stringResource(R.string.opponent)) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            isError = opponentError != null,
            supportingText = if (opponentError != null) {
                { Text(opponentError!!) }
            } else null,
        )

        AppTextField(
            modifier = Modifier.fillMaxWidth(),
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
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() }),
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
                                teamName = match?.teamName ?: "",
                                opponent = opponent,
                                location = location,
                                dateTime = match?.dateTime,
                                numberOfPeriods = match?.numberOfPeriods ?: 2,
                                squadCallUpIds = match?.squadCallUpIds ?: emptyList(),
                                captainId = match?.captainId,
                                startingLineupIds = selectedStartingLineup.toList(),
                                elapsedTimeMillis = match?.elapsedTimeMillis ?: 0L,
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
) {
    PlayerItem(
        player = player,
        showPositions = false,
        isSelected = isSelected,
        onMultiSelectionChange = onSelectionChange,
    )
}


@Preview(
    name = "Pixel 7 Pro",
    device = "spec:width=1440px,height=3120px,dpi=512",
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun DefaultPreview() {
    MaterialTheme {
        MatchForm(
            match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    teamName = "My Team",
                    opponent = "Rival Team",
                    location = "Home Stadium",
                    dateTime = System.currentTimeMillis(),
                    numberOfPeriods = 2,
                    squadCallUpIds = listOf(1L, 2L, 3L, 4L, 5L),
                    captainId = 1L,
                    startingLineupIds = listOf(1L, 2L, 3L),
                ),
            availablePlayers =
                listOf(
                    Player(id = 1L, firstName = "John", lastName = "Doe", number = 9, listOf(Position.Goalkeeper), teamId = 1, isCaptain = false),
                    Player(id = 2L, firstName = "Jane", lastName = "Smith", number = 10, listOf(Position.Defender), teamId = 1, isCaptain = false),
                    Player(id = 3L, firstName = "Mike", lastName = "Johnson", number = 11, listOf(Position.Midfielder), teamId = 1, isCaptain = false),
                    Player(id = 4L, firstName = "Emily", lastName = "Davis", number = 7, listOf(Position.Forward), teamId = 1, isCaptain = false),
                    Player(id = 5L, firstName = "David", lastName = "Wilson", number = 8, listOf(Position.Midfielder), teamId = 1, isCaptain = false),
                ),
            onSave = {},
            onCancel = {},
        )
    }
}
