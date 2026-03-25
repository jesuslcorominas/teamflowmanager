package com.jesuslcorominas.teamflowmanager.ui.matches.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.ui.components.AppTitle
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.DateFormatter
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.archive_match
import teamflowmanager.shared_ui.generated.resources.match_score
import teamflowmanager.shared_ui.generated.resources.unarchive_match

@Composable
fun PlayedMatchCard(
    modifier: Modifier = Modifier,
    match: Match,
    onNavigateToDetail: () -> Unit = {},
    onAction: () -> Unit = {},
) {
    AppCard(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onNavigateToDetail() },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TFMSpacing.spacing04),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AppTitle(title = match.opponent)
                Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                Text(
                    text = match.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                match.dateTime?.let {
                    Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                    Text(
                        text = DateFormatter.formatDateTime(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.match_score, match.goals, match.opponentGoals),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                IconButton(onClick = onAction) {
                    Icon(
                        imageVector = if (match.archived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription =
                            stringResource(
                                if (match.archived) Res.string.unarchive_match else Res.string.archive_match,
                            ),
                    )
                }
            }
        }
    }
}
