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
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.DateFormatter

@Composable
fun ArchivedMatchCard(
    modifier: Modifier = Modifier,
    match: Match,
    onNavigateToDetail: () -> Unit = {},
    onUnarchive: () -> Unit = {},
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigateToDetail() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = match.opponent,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                Text(
                    text = match.location ?: stringResource(R.string.location),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (match.dateTime != null) {
                    Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                    Text(
                        text = match.dateTime?.let { DateFormatter.formatDateTime(it) } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // TODO: Show actual score when score tracking is implemented
                Text(
                    text = stringResource(R.string.match_score, 0, 0),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                IconButton(onClick = onUnarchive) {
                    Icon(
                        imageVector = Icons.Default.Unarchive,
                        contentDescription = stringResource(R.string.unarchive_match),
                    )
                }
            }
        }
    }
}
