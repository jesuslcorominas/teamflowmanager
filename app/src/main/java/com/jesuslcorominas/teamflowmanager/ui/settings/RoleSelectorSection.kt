package com.jesuslcorominas.teamflowmanager.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
        Text(
            text = stringResource(R.string.settings_role_section),
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.padding(horizontal = TFMSpacing.spacing02),
        )

        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

        Column(modifier = Modifier.selectableGroup()) {
            RoleOption(
                label = stringResource(R.string.settings_role_president),
                selected = activeRole == ActiveViewRole.President,
                enabled = enabled,
                onClick = { onRoleSelected(ActiveViewRole.President) },
            )
            RoleOption(
                label = stringResource(R.string.settings_role_coach),
                selected = activeRole == ActiveViewRole.Coach,
                enabled = enabled,
                onClick = { onRoleSelected(ActiveViewRole.Coach) },
            )
        }
    }
}

@Composable
private fun RoleOption(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    onClick = onClick,
                    role = Role.RadioButton,
                )
                .padding(horizontal = TFMSpacing.spacing02, vertical = TFMSpacing.spacing01),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, enabled = enabled, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.padding(start = TFMSpacing.spacing02),
        )
    }
}
