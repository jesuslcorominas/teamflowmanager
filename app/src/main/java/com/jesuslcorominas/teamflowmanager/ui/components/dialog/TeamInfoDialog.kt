package com.jesuslcorominas.teamflowmanager.ui.components.dialog

import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMElevation

@Composable
fun TeamInfoDialog(
    team: Team,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = TFMElevation.level3,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(TFMSpacing.spacing06),
                verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing04),
            ) {
                Text(
                    text = stringResource(R.string.team_info),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                TeamInfoItem(
                    label = stringResource(R.string.team_name),
                    value = team.name,
                )

                TeamInfoItem(
                    label = stringResource(R.string.coach_name),
                    value = team.coachName,
                )

                TeamInfoItem(
                    label = stringResource(R.string.delegate_name),
                    value = team.delegateName,
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = TFMSpacing.spacing02),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(onClick = onEdit) {
                        Text(stringResource(R.string.edit))
                    }
                    Spacer(modifier = Modifier.width(TFMSpacing.spacing02))
                    Button(onClick = onDismiss) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}

@Composable
fun TeamInfoItem(
    label: String,
    value: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@Composable
private fun TeamInfoDialogPreview() {
    TFMAppTheme {
        TeamInfoDialog(
            team = Team(
                id = 1,
                name = "Team Name Example",
                coachName = "John Doe",
                delegateName = "Jane Smith",
            ),
            onDismiss = {},
            onEdit = {},
        )
    }
}
