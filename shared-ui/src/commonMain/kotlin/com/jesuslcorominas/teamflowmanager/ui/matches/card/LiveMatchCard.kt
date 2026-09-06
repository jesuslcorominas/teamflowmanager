package com.jesuslcorominas.teamflowmanager.ui.matches.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.ui.util.formatTime
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.first_half
import teamflowmanager.shared_ui.generated.resources.first_quarter
import teamflowmanager.shared_ui.generated.resources.fourth_quarter
import teamflowmanager.shared_ui.generated.resources.match_live_badge
import teamflowmanager.shared_ui.generated.resources.second_half
import teamflowmanager.shared_ui.generated.resources.second_quarter
import teamflowmanager.shared_ui.generated.resources.third_quarter

@Composable
fun LiveMatchCard(
    match: Match,
    currentTime: Long,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activePeriod = match.periods.firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }
    val periodLabel =
        activePeriod?.let { period ->
            when (match.periodType) {
                PeriodType.HALF_TIME ->
                    when (period.periodNumber) {
                        1 -> stringResource(Res.string.first_half)
                        else -> stringResource(Res.string.second_half)
                    }
                PeriodType.QUARTER_TIME ->
                    when (period.periodNumber) {
                        1 -> stringResource(Res.string.first_quarter)
                        2 -> stringResource(Res.string.second_quarter)
                        3 -> stringResource(Res.string.third_quarter)
                        else -> stringResource(Res.string.fourth_quarter)
                    }
            }
        } ?: ""
    val elapsedMillis = match.getTotalElapsed(currentTime)

    AppCard(modifier = modifier.clickable { onNavigateToDetail() }) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = match.opponent,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Box(
                    modifier =
                        Modifier
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.extraSmall,
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.match_live_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = "${match.goals} – ${match.opponentGoals}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatTime(elapsedMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (periodLabel.isNotBlank()) {
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
