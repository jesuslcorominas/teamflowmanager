package com.jesuslcorominas.teamflowmanager.ui.util

import androidx.compose.runtime.Composable
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.team_type_football

@Composable
fun TeamType.localizedName(): String = stringResource(Res.string.team_type_football, players)
