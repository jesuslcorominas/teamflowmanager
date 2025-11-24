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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.model.TeamType
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.toStringRes

@Composable
fun TeamDetailContent(team: Team, captain: Player? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TFMSpacing.spacing04),
        horizontalAlignment = Alignment.Start,
    ) {
        InfoRow(
            label = stringResource(R.string.team_name),
            value = team.name,
        )

        InfoRow(
            label = stringResource(R.string.coach_name),
            value = team.coachName,
        )

        InfoRow(
            label = stringResource(R.string.delegate_name),
            value = team.delegateName,
        )
        
        InfoRow(
            label = stringResource(R.string.team_type),
            value = stringResource(team.teamType.toStringRes()),
        )

        if (captain != null) {
            InfoRow(
                label = stringResource(R.string.team_captain),
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


@Preview(showBackground = true, widthDp = 512, heightDp = 1200, device = Devices.PIXEL_7_PRO)
@Composable
fun TeamDetailPreview() {
    TFMAppTheme {
        TeamDetailContent(
            team = Team(
                id = 1,
                name = "The Invincibles",
                coachName = "John Doe",
                delegateName = "Jane Smith",
                teamType = TeamType.FOOTBALL_11
            ),
            captain = Player(
                id = 4,
                firstName = "Alice",
                lastName = "Johnson",
                number = 10,
                positions = listOf(),
                isCaptain = true,
                teamId = 1
            )
        )
    }
}
