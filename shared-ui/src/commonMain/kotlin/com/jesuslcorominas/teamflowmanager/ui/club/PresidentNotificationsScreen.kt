package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.viewmodel.PresidentNotificationsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.close
import teamflowmanager.shared_ui.generated.resources.error_loading_members
import teamflowmanager.shared_ui.generated.resources.nav_notifications
import teamflowmanager.shared_ui.generated.resources.notifications_delete_description
import teamflowmanager.shared_ui.generated.resources.notifications_empty
import teamflowmanager.shared_ui.generated.resources.notifications_mark_read_description
import teamflowmanager.shared_ui.generated.resources.notifications_mark_unread_description
import teamflowmanager.shared_ui.generated.resources.notifications_no_club
import teamflowmanager.shared_ui.generated.resources.settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresidentNotificationsScreen(
    viewModel: PresidentNotificationsViewModel = koinViewModel(),
    onNavigateToClubSettings: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNotification by remember { mutableStateOf<PresidentNotification?>(null) }

    selectedNotification?.let { notification ->
        LaunchedEffect(notification.id) {
            viewModel.markAsRead(notification.id)
        }
        AlertDialog(
            onDismissRequest = { selectedNotification = null },
            title = { Text(text = notification.title) },
            text = { Text(text = notification.body) },
            confirmButton = {
                TextButton(onClick = { selectedNotification = null }) {
                    Text(text = stringResource(Res.string.close))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.nav_notifications),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToClubSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(Res.string.settings),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is PresidentNotificationsViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is PresidentNotificationsViewModel.UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.error_loading_members),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            is PresidentNotificationsViewModel.UiState.NoClubMembership -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.notifications_no_club),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            is PresidentNotificationsViewModel.UiState.Success -> {
                if (state.notifications.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.notifications_empty),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                    ) {
                        itemsIndexed(state.notifications, key = { _, it -> it.id }) { index, notification ->
                            NotificationItem(
                                notification = notification,
                                onMarkRead = { viewModel.markAsRead(notification.id) },
                                onMarkUnread = { viewModel.markAsUnread(notification.id) },
                                onDelete = { viewModel.deleteNotification(notification.id) },
                                onClick = { selectedNotification = notification },
                            )
                            if (index < state.notifications.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: PresidentNotification,
    onMarkRead: () -> Unit,
    onMarkUnread: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (notification.read) Icons.Default.Drafts else Icons.Default.Email,
            contentDescription = null,
            tint =
                if (notification.read) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                },
            modifier = Modifier.padding(end = 12.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold,
            )
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (notification.read) FontWeight.Normal else FontWeight.SemiBold,
            )
        }

        Row {
            if (!notification.read) {
                IconButton(onClick = onMarkRead) {
                    Icon(
                        imageVector = Icons.Default.Drafts,
                        contentDescription = stringResource(Res.string.notifications_mark_read_description),
                    )
                }
            } else {
                IconButton(onClick = onMarkUnread) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = stringResource(Res.string.notifications_mark_unread_description),
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.notifications_delete_description),
                )
            }
        }
    }
}