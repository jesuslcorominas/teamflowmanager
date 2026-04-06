package com.jesuslcorominas.teamflowmanager.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing

@Composable
fun RoleSelectorSection(
    activeRole: ActiveViewRole,
    enabled: Boolean,
    onRoleSelected: (ActiveViewRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TFMSpacing.spacing02),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_role_coach),
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = activeRole == ActiveViewRole.Coach,
                onCheckedChange = { isCoach ->
                    onRoleSelected(if (isCoach) ActiveViewRole.Coach else ActiveViewRole.President)
                },
                enabled = enabled,
            )
        }
    }
}
