package com.jesuslcorominas.teamflowmanager.ui.team.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.localizedName
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.coach_name
import teamflowmanager.shared_ui.generated.resources.delegate_name
import teamflowmanager.shared_ui.generated.resources.team_captain
import teamflowmanager.shared_ui.generated.resources.team_name
import teamflowmanager.shared_ui.generated.resources.team_type

@Composable
fun TeamDetailContent(team: Team, captain: Player? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TFMSpacing.spacing04),
        horizontalAlignment = Alignment.Start,
    ) {
        InfoRow(
            label = stringResource(Res.string.team_name),
            value = team.name,
        )

        InfoRow(
            label = stringResource(Res.string.coach_name),
            value = team.coachName,
        )

        InfoRow(
            label = stringResource(Res.string.delegate_name),
            value = team.delegateName,
        )

        InfoRow(
            label = stringResource(Res.string.team_type),
            value = team.teamType.localizedName(),
        )

        if (captain != null) {
            InfoRow(
                label = stringResource(Res.string.team_captain),
                value = "${captain.firstName} ${captain.lastName} (#${captain.number})",
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TFMSpacing.spacing03, horizontal = TFMSpacing.spacing02),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = TFMSpacing.spacing01),
        )
    }
}
