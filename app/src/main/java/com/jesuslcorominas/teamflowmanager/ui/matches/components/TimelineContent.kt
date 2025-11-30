package com.jesuslcorominas.teamflowmanager.ui.matches.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.ui.theme.Primary
import com.jesuslcorominas.teamflowmanager.ui.theme.SubstitutionGreen
import com.jesuslcorominas.teamflowmanager.ui.theme.SubstitutionRed
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.formatTime

@Composable
fun TimelineContent(
    events: List<TimelineEvent>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(TFMSpacing.spacing04),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        items(events) { event ->
            TimelineEventCard(event = event)
        }
    }
}

@Composable
private fun TimelineEventCard(event: TimelineEvent) {
    when (event) {
        is TimelineEvent.StartingLineup -> StartingLineupCard(event)
        is TimelineEvent.GoalScored -> GoalScoredCard(event)
        is TimelineEvent.Substitution -> SubstitutionEventCard(event)
        is TimelineEvent.Timeout -> TimeoutCard(event)
        is TimelineEvent.PeriodBreak -> PeriodBreakCard(event)
    }
}

@Composable
private fun StartingLineupCard(event: TimelineEvent.StartingLineup) {
    EventCard(
        timeMillis = event.matchElapsedTimeMillis,
        icon = Icons.Default.People,
        iconBackgroundColor = Primary,
        title = stringResource(R.string.timeline_starting_lineup),
    ) {
        val playerNames = event.players.joinToString(", ") { "${it.firstName} ${it.lastName}" }
        Text(
            text = playerNames,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GoalScoredCard(event: TimelineEvent.GoalScored) {
    val (title, iconBackgroundColor) = when {
        event.isOpponentGoal -> stringResource(R.string.timeline_opponent_goal) to SubstitutionRed
        else -> stringResource(R.string.timeline_goal) to SubstitutionGreen
    }

    EventCard(
        timeMillis = event.matchElapsedTimeMillis,
        icon = Icons.Default.SportsSoccer,
        iconBackgroundColor = iconBackgroundColor,
        title = title,
    ) {
        Column {
            if (!event.isOpponentGoal && event.scorer != null) {
                Text(
                    text = "${event.scorer.firstName} ${event.scorer.lastName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                Text(
                    text = "${event.teamScore}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                )
                Text(
                    text = "-",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${event.opponentScore}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SubstitutionRed,
                )
            }
        }
    }
}

@Composable
private fun SubstitutionEventCard(event: TimelineEvent.Substitution) {
    EventCard(
        timeMillis = event.matchElapsedTimeMillis,
        icon = Icons.Default.SwapHoriz,
        iconBackgroundColor = Primary,
        title = stringResource(R.string.timeline_substitution),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = SubstitutionGreen,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "${event.playerIn.firstName} ${event.playerIn.lastName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubstitutionGreen,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SubstitutionRed,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "${event.playerOut.firstName} ${event.playerOut.lastName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubstitutionRed,
                )
            }
        }
    }
}

@Composable
private fun TimeoutCard(event: TimelineEvent.Timeout) {
    EventCard(
        timeMillis = event.matchElapsedTimeMillis,
        icon = Icons.Default.Timer,
        iconBackgroundColor = MaterialTheme.colorScheme.tertiary,
        title = stringResource(R.string.timeline_timeout),
    )
}

@Composable
private fun PeriodBreakCard(event: TimelineEvent.PeriodBreak) {
    val title = when {
        event.periodType == PeriodType.HALF_TIME -> stringResource(R.string.timeline_halftime)
        else -> stringResource(R.string.timeline_quarter_break)
    }

    EventCard(
        timeMillis = event.matchElapsedTimeMillis,
        icon = Icons.Default.Pause,
        iconBackgroundColor = MaterialTheme.colorScheme.secondary,
        title = title,
    )
}

@Composable
private fun EventCard(
    timeMillis: Long,
    icon: ImageVector,
    iconBackgroundColor: Color,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing03),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Time badge
            Text(
                text = formatTime(timeMillis),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(56.dp),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.width(TFMSpacing.spacing03))

            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(TFMSpacing.spacing03))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (content != null) {
                    Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                    content()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineContentPreview() {
    val player1 = Player(
        id = 1L,
        firstName = "John",
        lastName = "Doe",
        number = 10,
        positions = listOf(Position.Forward),
        teamId = 1L,
        isCaptain = true,
    )
    val player2 = Player(
        id = 2L,
        firstName = "Jane",
        lastName = "Smith",
        number = 7,
        positions = listOf(Position.Midfielder),
        teamId = 1L,
        isCaptain = false,
    )
    val events = listOf(
        TimelineEvent.GoalScored(
            matchElapsedTimeMillis = 2700000L,
            scorer = player1,
            isOpponentGoal = false,
            teamScore = 3,
            opponentScore = 1,
        ),
        TimelineEvent.Substitution(
            matchElapsedTimeMillis = 1800000L,
            playerIn = player2,
            playerOut = player1,
        ),
        TimelineEvent.PeriodBreak(
            matchElapsedTimeMillis = 1500000L,
            periodNumber = 1,
            periodType = PeriodType.HALF_TIME,
        ),
        TimelineEvent.GoalScored(
            matchElapsedTimeMillis = 900000L,
            scorer = null,
            isOpponentGoal = true,
            teamScore = 2,
            opponentScore = 1,
        ),
        TimelineEvent.GoalScored(
            matchElapsedTimeMillis = 600000L,
            scorer = player1,
            isOpponentGoal = false,
            teamScore = 2,
            opponentScore = 0,
        ),
        TimelineEvent.GoalScored(
            matchElapsedTimeMillis = 300000L,
            scorer = player2,
            isOpponentGoal = false,
            teamScore = 1,
            opponentScore = 0,
        ),
        TimelineEvent.StartingLineup(
            matchElapsedTimeMillis = 0L,
            players = listOf(player1, player2),
        ),
    )

    TFMAppTheme {
        TimelineContent(events = events)
    }
}
