package com.jesuslcorominas.teamflowmanager.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

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

    // Handle sign out complete
    LaunchedEffect(signOutComplete) {
        if (signOutComplete) {
            viewModel.clearSignOutComplete()
            onSignOut()
        }
    }

    // Handle role change event — navigate to Splash so routing is re-evaluated
    LaunchedEffect(roleSelectorState.roleChangedEvent) {
        if (roleSelectorState.roleChangedEvent) {
            viewModel.onRoleChangedEventConsumed()
            onRoleChanged()
        }
    }

    // Sign out confirmation dialog
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
            title = {
                Text(text = stringResource(R.string.sign_out_title))
            },
            text = {
                Text(text = stringResource(R.string.sign_out_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.signOut()
                        showSignOutDialog = false
                    },
                ) {
                    Text(stringResource(R.string.sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
                    .navigationBarsPadding()
                    .padding(TFMSpacing.spacing04),
        ) {
            // Account section
            Text(
                text = stringResource(R.string.settings_account_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = TFMSpacing.spacing02),
            )

            Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

            // User info and sign out button
            currentUser?.let { user ->
                UserAccountItem(
                    user = user,
                    onClick = { showSignOutDialog = true },
                )
            }

            // Role selector — visible only when president is also assigned as coach to a team
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

            // Notification preferences — visible for any president-view club member
            if (roleSelectorState.activeRole == com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole.President &&
                notificationPreferences.clubId.isNotEmpty()
            ) {
                Spacer(modifier = Modifier.height(TFMSpacing.spacing04))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(TFMSpacing.spacing04))
                Text(
                    text = stringResource(R.string.settings_notifications_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = TFMSpacing.spacing02),
                )
                Spacer(modifier = Modifier.height(TFMSpacing.spacing02))
                NotificationSwitchItem(
                    title = stringResource(R.string.settings_notifications_match_events),
                    subtitle = when (notificationPreferences.matchEventsState) {
                        com.jesuslcorominas.teamflowmanager.domain.model.GlobalNotificationState.MIXED ->
                            stringResource(R.string.settings_notifications_mixed)
                        else -> stringResource(R.string.settings_notifications_applies_all_teams)
                    },
                    checked = notificationPreferences.matchEventsState == com.jesuslcorominas.teamflowmanager.domain.model.GlobalNotificationState.ALL_ON,
                    onCheckedChange = { viewModel.updateGlobalMatchEvents(it) },
                )
                Spacer(modifier = Modifier.height(TFMSpacing.spacing02))
                NotificationSwitchItem(
                    title = stringResource(R.string.settings_notifications_goals),
                    subtitle = when (notificationPreferences.goalsState) {
                        com.jesuslcorominas.teamflowmanager.domain.model.GlobalNotificationState.MIXED ->
                            stringResource(R.string.settings_notifications_mixed)
                        else -> stringResource(R.string.settings_notifications_applies_all_teams)
                    },
                    checked = notificationPreferences.goalsState == com.jesuslcorominas.teamflowmanager.domain.model.GlobalNotificationState.ALL_ON,
                    onCheckedChange = { viewModel.updateGlobalGoals(it) },
                )
            }

            Spacer(modifier = Modifier.height(TFMSpacing.spacing06))
        }
    }
}

@Composable
private fun UserAccountItem(
    user: com.jesuslcorominas.teamflowmanager.domain.model.User,
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
        // User photo or placeholder
        if (user.photoUrl != null) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = null,
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(modifier = Modifier.width(TFMSpacing.spacing04))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName ?: stringResource(R.string.user_name_unknown),
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
            contentDescription = stringResource(R.string.sign_out),
            tint = MaterialTheme.colorScheme.error,
        )
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
                .padding(horizontal = TFMSpacing.spacing02, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
