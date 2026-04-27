package com.jesuslcorominas.teamflowmanager.ui.team.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTextField
import com.jesuslcorominas.teamflowmanager.ui.components.form.ClearableRadioSelectorHeader
import com.jesuslcorominas.teamflowmanager.ui.components.form.ClearableRadioSelectorList
import com.jesuslcorominas.teamflowmanager.ui.components.form.SelectableItem
import com.jesuslcorominas.teamflowmanager.ui.main.LocalContentBottomPadding
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.localizedName
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.ic_launcher
import teamflowmanager.shared_ui.generated.resources.captain_selection_title
import teamflowmanager.shared_ui.generated.resources.coach_name
import teamflowmanager.shared_ui.generated.resources.create_team_title
import teamflowmanager.shared_ui.generated.resources.delegate_name
import teamflowmanager.shared_ui.generated.resources.delegate_name_required
import teamflowmanager.shared_ui.generated.resources.first_name_required
import teamflowmanager.shared_ui.generated.resources.save
import teamflowmanager.shared_ui.generated.resources.team_name
import teamflowmanager.shared_ui.generated.resources.team_name_required
import teamflowmanager.shared_ui.generated.resources.team_type

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeamForm(
    team: Team? = null,
    players: List<Player> = listOf(),
    clubNumericId: Long? = null,
    clubId: String? = null,
    isPresident: Boolean = false,
    onShowTeamTypeChangeError: () -> Unit = {},
    onSave: (Team, Long?) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var formState by remember {
        val baseState = team.toTeamFormState()
        mutableStateOf(
            baseState.copy(
                clubNumericId = clubNumericId ?: baseState.clubNumericId,
                clubId = clubId ?: baseState.clubId,
            ),
        )
    }

    var selectedOption by remember { mutableStateOf(players.firstOrNull { it.isCaptain }?.id) }

    var teamTypeExpanded by remember { mutableStateOf(false) }

    val validateAndSave = {
        formState =
            formState.copy(
                errors =
                    FormErrors(
                        name = formState.name.isBlank(),
                        coachName = formState.coachName.isBlank(),
                        delegateName = formState.delegateName.isBlank(),
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
                modifier =
                    Modifier
                        .weight(1F)
                        .padding(TFMSpacing.spacing04),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (team == null) {
                    item {
                        Icon(
                            modifier = Modifier.size(144.dp),
                            painter = painterResource(Res.drawable.ic_launcher),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )

                        Text(
                            modifier = Modifier.padding(bottom = TFMSpacing.spacing04),
                            text = stringResource(Res.string.create_team_title),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                }

                item {
                    AppTextField(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = TFMSpacing.spacing03),
                        value = formState.name,
                        onValueChange = {
                            formState =
                                formState.copy(
                                    name = it,
                                    errors = formState.errors.copy(name = false),
                                )
                        },
                        label = { Text(stringResource(Res.string.team_name)) },
                        isError = formState.errors.name,
                        supportingText =
                            if (formState.errors.name) {
                                { Text(stringResource(Res.string.team_name_required)) }
                            } else {
                                null
                            },
                        keyboardOptions =
                            KeyboardOptions(
                                imeAction = ImeAction.Next,
                                capitalization = KeyboardCapitalization.Words,
                            ),
                        keyboardActions =
                            KeyboardActions(onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            }),
                    )
                }

                item {
                    AppTextField(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = TFMSpacing.spacing03),
                        value = formState.coachName,
                        onValueChange = {
                            formState =
                                formState.copy(
                                    coachName = it,
                                    errors = formState.errors.copy(coachName = false),
                                )
                        },
                        label = { Text(stringResource(Res.string.coach_name)) },
                        isError = formState.errors.coachName,
                        supportingText =
                            if (formState.errors.coachName) {
                                { Text(stringResource(Res.string.first_name_required)) }
                            } else {
                                null
                            },
                        keyboardOptions =
                            KeyboardOptions(
                                imeAction = ImeAction.Next,
                                capitalization = KeyboardCapitalization.Words,
                            ),
                        keyboardActions =
                            KeyboardActions(onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            }),
                    )
                }

                item {
                    AppTextField(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = TFMSpacing.spacing03),
                        value = formState.delegateName,
                        onValueChange = {
                            formState =
                                formState.copy(
                                    delegateName = it,
                                    errors = formState.errors.copy(delegateName = false),
                                )
                        },
                        label = { Text(stringResource(Res.string.delegate_name)) },
                        isError = formState.errors.delegateName,
                        supportingText =
                            if (formState.errors.delegateName) {
                                { Text(stringResource(Res.string.delegate_name_required)) }
                            } else {
                                null
                            },
                        keyboardOptions =
                            KeyboardOptions(
                                imeAction = ImeAction.Done,
                                capitalization = KeyboardCapitalization.Words,
                            ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() }),
                    )
                }

                item {
                    TeamTypeDropdown(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = TFMSpacing.spacing04),
                        selectedType = formState.teamType,
                        expanded = teamTypeExpanded,
                        onExpandedChange = { teamTypeExpanded = !teamTypeExpanded },
                        onTypeSelected = { type ->
                            formState = formState.copy(teamType = type)
                            teamTypeExpanded = false
                        },
                    )
                }

                if (team != null && players.isNotEmpty()) {
                    stickyHeader {
                        ClearableRadioSelectorHeader(
                            title = stringResource(Res.string.captain_selection_title),
                            selectedOption = selectedOption,
                            onClear = {
                                selectedOption = null
                                formState = formState.copy()
                            },
                        )
                    }

                    val selectablePlayers =
                        players.map {
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
                            },
                        )
                    }
                }
            }

            Button(
                modifier =
                    Modifier
                        .padding(
                            start = TFMSpacing.spacing04,
                            end = TFMSpacing.spacing04,
                            bottom = LocalContentBottomPadding.current + 8.dp,
                        )
                        .fillMaxWidth(),
                onClick = { validateAndSave() },
            ) {
                Text(text = stringResource(Res.string.save))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TeamTypeDropdown(
    selectedType: TeamType,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onTypeSelected: (TeamType) -> Unit,
    modifier: Modifier = Modifier,
) {
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        AppTextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            value = selectedType.localizedName(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.team_type)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            TeamType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.localizedName()) },
                    onClick = { onTypeSelected(type) },
                )
            }
        }
    }
}

private data class FormErrors(
    val name: Boolean = false,
    val coachName: Boolean = false,
    val delegateName: Boolean = false,
) {
    val hasErrors: Boolean = name || coachName || delegateName
}

private data class TeamFormState(
    val id: Long = 0,
    val name: String = "",
    val coachName: String = "",
    val delegateName: String = "",
    val teamType: TeamType = TeamType.FOOTBALL_5,
    val coachId: String? = null,
    val clubNumericId: Long? = null,
    val clubId: String? = null,
    val errors: FormErrors = FormErrors(),
)

private fun TeamFormState.toTeam(): Team =
    Team(
        id = id,
        name = name.trimEnd(),
        coachName = coachName.trimEnd(),
        delegateName = delegateName.trimEnd(),
        teamType = teamType,
        coachId = coachId,
        clubId = clubNumericId,
        clubRemoteId = clubId,
    )

private fun Team?.toTeamFormState() =
    this?.let {
        TeamFormState(
            id = it.id,
            name = it.name,
            coachName = it.coachName,
            delegateName = it.delegateName,
            teamType = it.teamType,
            coachId = it.coachId,
            clubNumericId = it.clubId,
            clubId = it.clubRemoteId,
        )
    } ?: TeamFormState()
