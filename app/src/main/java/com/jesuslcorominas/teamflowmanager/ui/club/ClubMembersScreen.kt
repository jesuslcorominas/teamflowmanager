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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.viewmodel.ClubMembersViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ClubMembersScreen(
    viewModel: ClubMembersViewModel = koinViewModel(),
    onMemberClick: (ClubMember) -> Unit = {}
) {
    TrackScreenView(screenName = ScreenName.CLUB_MEMBERS, screenClass = "ClubMembersScreen")

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ClubMembersViewModel.UiState.Loading -> {
            Loading()
        }
        is ClubMembersViewModel.UiState.Success -> {
            if (state.members.isEmpty()) {
                EmptyMembersMessage(modifier = Modifier.fillMaxSize())
            } else {
                MembersListContent(
                    members = state.members,
                    modifier = Modifier.fillMaxSize(),
                    onMemberClick = onMemberClick
                )
            }
        }
        is ClubMembersViewModel.UiState.Error -> {
            ErrorMessage(
                message = stringResource(R.string.error_loading_members),
                modifier = Modifier.fillMaxSize()
            )
        }
        is ClubMembersViewModel.UiState.NoClubMembership -> {
            ErrorMessage(
                message = stringResource(R.string.no_club_membership_error),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EmptyMembersMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.no_members_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MembersListContent(
    members: List<ClubMember>,
    modifier: Modifier = Modifier,
    onMemberClick: (ClubMember) -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(members, key = { it.id }) { member ->
            MemberCard(
                member = member,
                onClick = { onMemberClick(member) }
            )
        }
    }
}

@Composable
private fun MemberCard(
    member: ClubMember,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = member.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}
