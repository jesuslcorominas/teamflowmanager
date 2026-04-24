package com.jesuslcorominas.teamflowmanager.ui.team

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.assign_coach_confirm
import teamflowmanager.shared_ui.generated.resources.assign_coach_dialog_title
import teamflowmanager.shared_ui.generated.resources.assign_coach_email_placeholder
import teamflowmanager.shared_ui.generated.resources.assign_coach_error
import teamflowmanager.shared_ui.generated.resources.assign_coach_not_member
import teamflowmanager.shared_ui.generated.resources.assign_coach_tab_email
import teamflowmanager.shared_ui.generated.resources.assign_coach_tab_members
import teamflowmanager.shared_ui.generated.resources.cancel
import teamflowmanager.shared_ui.generated.resources.no_results

private const val TAB_MEMBERS = 0
private const val TAB_EMAIL = 1
private val MEMBERS_LIST_HEIGHT = 240.dp

@Composable
internal fun AssignCoachDialog(
    team: Team,
    members: List<ClubMember>,
    assignedCoachIds: Set<String>,
    error: String?,
    onDismiss: () -> Unit,
    onAssignMember: (ClubMember) -> Unit,
    onAssignByEmail: (String) -> Unit,
    onClearError: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(TAB_MEMBERS) }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.medium,
        title = {
            Text(stringResource(Res.string.assign_coach_dialog_title, team.name))
        },
        text = {
            Column {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == TAB_MEMBERS,
                        onClick = {
                            selectedTab = TAB_MEMBERS
                            onClearError()
                        },
                        text = { Text(stringResource(Res.string.assign_coach_tab_members)) },
                    )
                    Tab(
                        selected = selectedTab == TAB_EMAIL,
                        onClick = {
                            selectedTab = TAB_EMAIL
                            onClearError()
                        },
                        text = { Text(stringResource(Res.string.assign_coach_tab_email)) },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                when (selectedTab) {
                    TAB_MEMBERS -> {
                        val assignableMembers = members.filter { it.userId !in assignedCoachIds }
                        if (assignableMembers.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.no_results),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            LazyColumn(modifier = Modifier.height(MEMBERS_LIST_HEIGHT)) {
                                items(assignableMembers, key = { it.userId }) { member ->
                                    Column(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable { onAssignMember(member) }
                                                .padding(vertical = 12.dp, horizontal = 4.dp),
                                    ) {
                                        Text(
                                            text = member.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            text = member.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                    TAB_EMAIL -> {
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                onClearError()
                            },
                            label = { Text(stringResource(Res.string.assign_coach_email_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = error != null,
                            supportingText =
                                if (error != null) {
                                    {
                                        Text(
                                            text =
                                                if (error == "NO_MEMBER") {
                                                    stringResource(Res.string.assign_coach_not_member)
                                                } else {
                                                    stringResource(Res.string.assign_coach_error)
                                                },
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                } else {
                                    null
                                },
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == TAB_EMAIL) {
                Button(
                    onClick = { onAssignByEmail(email) },
                    enabled = email.isNotBlank(),
                ) {
                    Text(stringResource(Res.string.assign_coach_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}
