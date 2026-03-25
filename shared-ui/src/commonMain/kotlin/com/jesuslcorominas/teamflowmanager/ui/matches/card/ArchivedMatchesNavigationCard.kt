package com.jesuslcorominas.teamflowmanager.ui.matches.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.archived_matches

@Composable
fun ArchivedMatchesNavigationCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TFMSpacing.spacing04, vertical = TFMSpacing.spacing02),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.padding(start = TFMSpacing.spacing03))
                Text(
                    text = stringResource(Res.string.archived_matches),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
