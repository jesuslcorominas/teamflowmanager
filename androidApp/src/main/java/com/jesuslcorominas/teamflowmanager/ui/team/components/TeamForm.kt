package com.jesuslcorominas.teamflowmanager.ui.team.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTextField
import com.jesuslcorominas.teamflowmanager.ui.components.form.ClearableRadioSelectorHeader
import com.jesuslcorominas.teamflowmanager.ui.components.form.ClearableRadioSelectorList
import com.jesuslcorominas.teamflowmanager.ui.components.form.SelectableItem
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.toStringRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamForm(team: Team? = null, players: List<Player> = listOf(), onShowTeamTypeChangeError: () -> Unit = {}, onSave: (Team, Long?) -> Unit) {
    val focusManager = LocalFocusManager.current
    var formState by remember { mutableStateOf(team.toTeamFormState()) }

    var selectedOption by remember { mutableStateOf(players.firstOrNull { it.isCaptain }?.id) }

    var teamTypeExpanded by remember { mutableStateOf(false) }

    val validateAndSave = {
        formState = formState.copy(
            errors = FormErrors(
                name = formState.name.isBlank(),
                coachName = formState.coachName.isBlank(),
                delegateName = formState.delegateName.isBlank()
            ),
        )

        if (!formState.errors.hasErrors) {
            onSave(formState.toTeam(), selectedOption)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            LazyColumn(
                modifier = Modifier
                    .weight(1F)
                    .padding(TFMSpacing.spacing04),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (team == null) {
                    item {
                        Icon(
                            modifier = Modifier.size(TFMSpacing.spacing18),
                            painter = painterResource(id = R.drawable.ic_launcher),
                            contentDescription = stringResource(R.string.app_name),
                            tint = Color.Unspecified
                        )

                        Text(
                            modifier = Modifier.padding(
                                bottom = TFMSpacing.spacing04
                            ),
                            text = stringResource(id = R.string.create_team_title),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                }

                item {
                    AppTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = TFMSpacing.spacing03),
                        value = formState.name,
                        onValueChange = {
                            formState = formState.copy(
                                name = it,
                                errors = formState.errors.copy(name = false)
                            )
                        },
                        label = { Text(stringResource(R.string.team_name)) },
                        isError = formState.errors.name,
                        supportingText = if (formState.errors.name) {
                            { Text(stringResource(R.string.team_name_required)) }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Words
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }),
                    )
                }

                item {
                    AppTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = TFMSpacing.spacing03),
                        value = formState.coachName,
                        onValueChange = {
                            formState = formState.copy(
                                coachName = it,
                                errors = formState.errors.copy(coachName = false)
                            )
                        },
                        label = { Text(stringResource(R.string.coach_name)) },
                        isError = formState.errors.coachName,
                        supportingText = if (formState.errors.coachName) {
                            { Text(stringResource(R.string.first_name_required)) }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Words
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }),
                    )
                }

                item {
                    AppTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = TFMSpacing.spacing03),
                        value = formState.delegateName,
                        onValueChange = {
                            formState = formState.copy(
                                delegateName = it,
                                errors = formState.errors.copy(delegateName = false)
                            )
                        },
                        label = { Text(stringResource(R.string.delegate_name)) },
                        isError = formState.errors.delegateName,
                        supportingText = if (formState.errors.delegateName) {
                            { Text(stringResource(R.string.delegate_name_required)) }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            capitalization = KeyboardCapitalization.Words
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() }),
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = TFMSpacing.spacing04),
                        expanded = teamTypeExpanded,
                        onExpandedChange = { teamTypeExpanded = !teamTypeExpanded }
                    ) {
                        AppTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            value = stringResource(formState.teamType.toStringRes(), formState.teamType.players),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.team_type)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = teamTypeExpanded)
                            },
                        )

                        ExposedDropdownMenu(
                            expanded = teamTypeExpanded,
                            onDismissRequest = { teamTypeExpanded = false }
                        ) {
                            TeamType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(type.toStringRes(), type.players)) },
                                    onClick = {
                                        formState = formState.copy(teamType = type)
                                        teamTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (team != null && players.isNotEmpty()) {
                    stickyHeader {
                        ClearableRadioSelectorHeader(
                            title = stringResource(R.string.captain_selection_title),
                            selectedOption = selectedOption,
                            onClear = {
                                selectedOption = null
                                formState = formState.copy()
                            }
                        )
                    }

                    val selectablePlayers = players.map {
                        object : SelectableItem {
                            override val id = it.id
                            override val label = "${it.firstName} ${it.lastName}"
                        }
                    }

                    items(selectablePlayers) { item ->
                        ClearableRadioSelectorList(
                            items = listOf(item),
                            selectedOption = selectedOption,
                            onSelect = { id ->
                                selectedOption = id
                                formState = formState.copy()
                            }
                        )
                    }
                }
            }

            Button(
                modifier = Modifier
                    .padding(start = TFMSpacing.spacing04, end = TFMSpacing.spacing04, bottom = TFMSpacing.spacing04)
                    .fillMaxWidth(),
                onClick = { validateAndSave() },
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}

private data class FormErrors(
    val name: Boolean = false,
    val coachName: Boolean = false,
    val delegateName: Boolean = false
) {
    val hasErrors: Boolean = name || coachName || delegateName
}

private data class TeamFormState(
    val id: Long = 0,
    val name: String = "",
    val coachName: String = "",
    val delegateName: String = "",
    val teamType: TeamType = TeamType.FOOTBALL_5,
    val errors: FormErrors = FormErrors()
)

private fun TeamFormState.toTeam(): Team = Team(
    id = id,
    name = name.trimEnd(),
    coachName = coachName.trimEnd(),
    delegateName = delegateName.trimEnd(),
    teamType = teamType
)

private fun Team?.toTeamFormState() = this?.let {
    TeamFormState(
        id = it.id,
        name = it.name,
        coachName = it.coachName,
        delegateName = it.delegateName,
        teamType = it.teamType
    )
} ?: TeamFormState()

@Preview
@Composable
private fun TeamFormPreview() {
    TFMAppTheme {
        TeamForm(
            team = Team(
                id = 1,
                name = "Team A",
                coachName = "Coach A",
                delegateName = "Delegate A",
                teamType = TeamType.FOOTBALL_5
            ),
            players = listOf(
                Player(1, "John", "Doe", 3, listOf(), isCaptain = true, teamId = 1),
                Player(2, "Jane", "Smith", 2, listOf(), isCaptain = false, teamId = 1),
                Player(3, "Bob", "Johnson", 17, listOf(), isCaptain = false, teamId = 1),
            ),
            onSave = {_, _ -> }
        )
    }
}
