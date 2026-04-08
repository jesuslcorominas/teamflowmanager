package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.viewmodel.PresidentNotificationsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.error_loading_members
import teamflowmanager.shared_ui.generated.resources.notifications_delete_description
import teamflowmanager.shared_ui.generated.resources.notifications_empty
import teamflowmanager.shared_ui.generated.resources.notifications_mark_read_description
import teamflowmanager.shared_ui.generated.resources.notifications_mark_unread_description
import teamflowmanager.shared_ui.generated.resources.notifications_no_club

@Composable
fun PresidentNotificationsScreen(viewModel: PresidentNotificationsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is PresidentNotificationsViewModel.UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PresidentNotificationsViewModel.UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(Res.string.error_loading_members),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        is PresidentNotificationsViewModel.UiState.NoClubMembership -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(Res.string.notifications_no_club),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        is PresidentNotificationsViewModel.UiState.Success -> {
            if (state.notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.notifications_empty),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.notifications, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onMarkRead = { viewModel.markAsRead(notification.id) },
                            onMarkUnread = { viewModel.markAsUnread(notification.id) },
                            onDelete = { viewModel.deleteNotification(notification.id) },
                        )
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
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
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
}
