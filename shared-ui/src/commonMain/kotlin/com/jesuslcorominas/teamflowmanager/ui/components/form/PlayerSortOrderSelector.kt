package com.jesuslcorominas.teamflowmanager.ui.components.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.sort_by_active
import teamflowmanager.shared_ui.generated.resources.sort_by_number
import teamflowmanager.shared_ui.generated.resources.sort_by_time_asc
import teamflowmanager.shared_ui.generated.resources.sort_by_time_desc
import teamflowmanager.shared_ui.generated.resources.sort_players

enum class PlayerSortOrderBy {
    BY_NUMBER,
    BY_TIME_DESC,
    BY_TIME_ASC,
    BY_ACTIVE_FIRST,
}

@Composable
fun PlayerSortOrderSelector(
    availableSorts: List<PlayerSortOrderBy> = PlayerSortOrderBy.entries,
    currentSortOrder: PlayerSortOrderBy,
    onSortOrderChange: (PlayerSortOrderBy) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                Text(
                    text = stringResource(currentSortOrder.toStringRes()),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.AutoMirrored.Default.Sort,
                    contentDescription = stringResource(Res.string.sort_players),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            availableSorts.forEach { order ->
                DropdownMenuItem(
                    text = { Text(stringResource(order.toStringRes())) },
                    onClick = {
                        onSortOrderChange(order)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PlayerSortOrderBy.toStringRes() =
    when (this) {
        PlayerSortOrderBy.BY_NUMBER -> Res.string.sort_by_number
        PlayerSortOrderBy.BY_ACTIVE_FIRST -> Res.string.sort_by_active
        PlayerSortOrderBy.BY_TIME_DESC -> Res.string.sort_by_time_desc
        PlayerSortOrderBy.BY_TIME_ASC -> Res.string.sort_by_time_asc
    }
