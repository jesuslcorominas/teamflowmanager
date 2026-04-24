package com.jesuslcorominas.teamflowmanager.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.model.GlobalNotificationState
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.cancel
import teamflowmanager.shared_ui.generated.resources.settings_account_section
import teamflowmanager.shared_ui.generated.resources.settings_notifications_applies_all_teams
import teamflowmanager.shared_ui.generated.resources.settings_notifications_goals
import teamflowmanager.shared_ui.generated.resources.settings_notifications_match_events
import teamflowmanager.shared_ui.generated.resources.settings_notifications_mixed
import teamflowmanager.shared_ui.generated.resources.settings_notifications_section
import teamflowmanager.shared_ui.generated.resources.settings_role_coach
import teamflowmanager.shared_ui.generated.resources.sign_out
import teamflowmanager.shared_ui.generated.resources.sign_out_message
import teamflowmanager.shared_ui.generated.resources.sign_out_title
import teamflowmanager.shared_ui.generated.resources.user_name_unknown

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onSignOut: () -> Unit = {},
    onRoleChanged: () -> Unit = {},
) {
    TrackScreenView(screenName = ScreenName.SETTINGS, screenClass = "SettingsScreen")

    val currentUser by viewModel.currentUser.collectAsState()
    val signOutComplete by viewModel.signOutComplete.collectAsState()
    val roleSelectorState by viewModel.roleSelectorState.collectAsState()
    val notificationPreferences by viewModel.notificationPreferences.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(signOutComplete) {
        if (signOutComplete) {
            viewModel.clearSignOutComplete()
            onSignOut()
        }
    }

    LaunchedEffect(roleSelectorState.roleChangedEvent) {
        if (roleSelectorState.roleChangedEvent) {
            viewModel.onRoleChangedEventConsumed()
            onRoleChanged()
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = { Text(text = stringResource(Res.string.sign_out_title)) },
            text = { Text(text = stringResource(Res.string.sign_out_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.signOut()
                        showSignOutDialog = false
                    },
                ) {
                    Text(stringResource(Res.string.sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(TFMSpacing.spacing04),
        ) {
            Text(
                text = stringResource(Res.string.settings_account_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = TFMSpacing.spacing02),
            )

            Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

            currentUser?.let { user ->
                UserAccountItem(
                    user = user,
                    onClick = { showSignOutDialog = true },
                )
            }

            if (roleSelectorState.showRoleSelector) {
                Spacer(modifier = Modifier.height(TFMSpacing.spacing06))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(TFMSpacing.spacing06))
                RoleSelectorSection(
                    activeRole = roleSelectorState.activeRole,
                    enabled = roleSelectorState.isRoleSelectorEnabled,
                    onRoleSelected = { viewModel.onRoleSelected(it) },
                )
            }

            if (roleSelectorState.activeRole == ActiveViewRole.President && notificationPreferences.clubId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(TFMSpacing.spacing04))
                Text(
                    text = stringResource(Res.string.settings_notifications_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = TFMSpacing.spacing02),
                )
                Spacer(modifier = Modifier.height(TFMSpacing.spacing02))
                NotificationSwitchItem(
                    title = stringResource(Res.string.settings_notifications_match_events),
                    subtitle =
                        when (notificationPreferences.matchEventsState) {
                            GlobalNotificationState.MIXED -> stringResource(Res.string.settings_notifications_mixed)
                            else -> stringResource(Res.string.settings_notifications_applies_all_teams)
                        },
                    checked = notificationPreferences.matchEventsState == GlobalNotificationState.ALL_ON,
                    onCheckedChange = { viewModel.updateGlobalMatchEvents(it) },
                )
                Spacer(modifier = Modifier.height(TFMSpacing.spacing02))
                NotificationSwitchItem(
                    title = stringResource(Res.string.settings_notifications_goals),
                    subtitle =
                        when (notificationPreferences.goalsState) {
                            GlobalNotificationState.MIXED -> stringResource(Res.string.settings_notifications_mixed)
                            else -> stringResource(Res.string.settings_notifications_applies_all_teams)
                        },
                    checked = notificationPreferences.goalsState == GlobalNotificationState.ALL_ON,
                    onCheckedChange = { viewModel.updateGlobalGoals(it) },
                )
            }

            Spacer(modifier = Modifier.height(TFMSpacing.spacing06))
        }
    }
}

@Composable
private fun NotificationSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = TFMSpacing.spacing02, vertical = TFMSpacing.spacing01),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun UserAccountItem(
    user: User,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(TFMSpacing.spacing02),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(
            photoUrl = user.photoUrl,
            modifier = Modifier.size(48.dp),
        )

        Spacer(modifier = Modifier.width(TFMSpacing.spacing04))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName ?: stringResource(Res.string.user_name_unknown),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = user.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = stringResource(Res.string.sign_out),
            tint = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun RoleSelectorSection(
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
                text = stringResource(Res.string.settings_role_coach),
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
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
