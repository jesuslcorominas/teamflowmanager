package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.main.LocalContentBottomPadding
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.viewmodel.ClubMembersViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.error_loading_members
import teamflowmanager.shared_ui.generated.resources.expel_member_cancel
import teamflowmanager.shared_ui.generated.resources.expel_member_confirm
import teamflowmanager.shared_ui.generated.resources.expel_member_dialog_message
import teamflowmanager.shared_ui.generated.resources.expel_member_dialog_title
import teamflowmanager.shared_ui.generated.resources.no_club_membership_error
import teamflowmanager.shared_ui.generated.resources.no_members_message

@Composable
fun ClubMembersScreen(
    viewModel: ClubMembersViewModel = koinViewModel(),
    onMemberClick: (ClubMember) -> Unit = {},
) {
    TrackScreenView(screenName = ScreenName.CLUB_MEMBERS, screenClass = "ClubMembersScreen")

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ClubMembersViewModel.UiState.Loading -> Loading()
        is ClubMembersViewModel.UiState.Success -> {
            if (state.members.isEmpty()) {
                EmptyMembersMessage(modifier = Modifier.fillMaxSize())
            } else {
                MembersListContent(
                    members = state.members,
                    modifier = Modifier.fillMaxSize(),
                    onMemberClick = onMemberClick,
                    currentUserId = state.currentUserId,
                    showExpelAction = state.currentUserIsPresident,
                    onExpelMember = { member ->
                        viewModel.expelMember(member.userId, state.clubRemoteId)
                    },
                )
            }
        }
        is ClubMembersViewModel.UiState.Error -> {
            ErrorMessage(
                message = stringResource(Res.string.error_loading_members),
                modifier = Modifier.fillMaxSize(),
            )
        }
        is ClubMembersViewModel.UiState.NoClubMembership -> {
            ErrorMessage(
                message = stringResource(Res.string.no_club_membership_error),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun EmptyMembersMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.no_members_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MembersListContent(
    members: List<ClubMember>,
    modifier: Modifier = Modifier,
    currentUserId: String = "",
    showExpelAction: Boolean = false,
    onMemberClick: (ClubMember) -> Unit,
    onExpelMember: (ClubMember) -> Unit = {},
) {
    var memberToExpel by remember { mutableStateOf<ClubMember?>(null) }

    memberToExpel?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToExpel = null },
            title = { Text(stringResource(Res.string.expel_member_dialog_title)) },
            text = {
                Text(
                    stringResource(Res.string.expel_member_dialog_message, member.name),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onExpelMember(member)
                        memberToExpel = null
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.expel_member_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToExpel = null }) {
                    Text(stringResource(Res.string.expel_member_cancel))
                }
            },
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding =
            androidx.compose.foundation.layout.PaddingValues(
                bottom = LocalContentBottomPadding.current,
                top = TFMSpacing.spacing04,
                start = TFMSpacing.spacing04,
                end = TFMSpacing.spacing04,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(members, key = { it.id }) { member ->
            MemberCard(
                member = member,
                onClick = { onMemberClick(member) },
                showExpelAction = showExpelAction && member.userId != currentUserId,
                onExpelClick = { memberToExpel = member },
            )
        }
    }
}

@Composable
private fun MemberCard(
    member: ClubMember,
    onClick: () -> Unit,
    showExpelAction: Boolean = false,
    onExpelClick: () -> Unit = {},
) {
    AppCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = member.roles.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (showExpelAction) {
                IconButton(onClick = onExpelClick) {
                    Icon(
                        imageVector = Icons.Default.PersonRemove,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
